package campsite.service;

import campsite.model.Booking;
import campsite.model.BookingPreferences;
import campsite.model.CampingArea;
import campsite.model.Campsite;
import campsite.model.Guest;
import campsite.model.Plot;
import campsite.model.PlotType;
import campsite.structures.MyLinkedList;
import campsite.structures.Node;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

/**
 * Saves the whole system to a file and loads it back.
 *
 * <p>The file is binary, written with DataOutputStream. It is written by walking our own
 * linked lists, so no Java collections or arrays are needed. (Java's built-in object
 * serialization would also work, but it follows the node chain recursively and can overflow
 * the stack for long lists.)
 *
 * <p>File layout, in order: a magic number and version, the two id counters, all campsites
 * (with their areas and plots), then all guests with their bookings. Plots do not store their
 * bookings; the bookings are stored once, under the guest, and are re-attached to their plot
 * when the file is loaded.
 */
public final class PersistenceService {

    private static final int MAGIC = 0x43414D50; // the letters "CAMP"
    private static final int VERSION = 1;

    // How a booking's plot is stored
    private static final byte PLOT_NONE = 0;
    private static final byte PLOT_ATTACHED = 1;   // plot still exists: store only its identifier
    private static final byte PLOT_DETACHED = 2;   // plot was removed: keep a copy for the history

    private PersistenceService() {
    }

    // ------------------------------------------------------------------
    // Saving
    // ------------------------------------------------------------------

    /**
     * Saves the system. The data is written to a temporary file first and then moved over
     * the target, so a failure half-way never destroys an existing save file.
     *
     * @throws IOException if the file cannot be written
     */
    public static void save(CampsiteSystem system, Path file) throws IOException {
        Path folder = file.toAbsolutePath().getParent();
        if (folder != null) {
            Files.createDirectories(folder);
        }
        Path temp = file.toAbsolutePath().resolveSibling(file.getFileName() + ".tmp");
        try {
            try (DataOutputStream out =
                         new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(temp)))) {
                writeSystem(out, system);
            }
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Files.deleteIfExists(temp);
            throw e;
        }
    }

    private static void writeSystem(DataOutputStream out, CampsiteSystem system) throws IOException {
        out.writeInt(MAGIC);
        out.writeInt(VERSION);
        out.writeInt(system.getNextGuestId());
        out.writeInt(system.getNextBookingId());

        out.writeInt(system.getCampsites().size());
        Node<Campsite> campsiteNode = system.getCampsites().getHead();
        while (campsiteNode != null) {
            writeCampsite(out, campsiteNode.getData());
            campsiteNode = campsiteNode.getNext();
        }

        out.writeInt(system.getGuests().size());
        Node<Guest> guestNode = system.getGuests().getHead();
        while (guestNode != null) {
            writeGuest(out, system, guestNode.getData());
            guestNode = guestNode.getNext();
        }
    }

    private static void writeCampsite(DataOutputStream out, Campsite campsite) throws IOException {
        out.writeUTF(campsite.getName());
        writeString(out, campsite.getAddress());
        out.writeInt(campsite.getStars());
        writeString(out, campsite.getContactInfo());
        writeStringList(out, campsite.getFacilities());

        out.writeInt(campsite.getAreas().size());
        Node<CampingArea> areaNode = campsite.getAreas().getHead();
        while (areaNode != null) {
            CampingArea area = areaNode.getData();
            out.writeUTF(area.getIdentifier());
            writeString(out, area.getDescription());
            out.writeInt(area.getPlots().size());
            Node<Plot> plotNode = area.getPlots().getHead();
            while (plotNode != null) {
                writePlot(out, plotNode.getData());
                plotNode = plotNode.getNext();
            }
            areaNode = areaNode.getNext();
        }
    }

    private static void writePlot(DataOutputStream out, Plot plot) throws IOException {
        out.writeUTF(plot.getIdentifier());
        out.writeUTF(plot.getType().name());
        out.writeBoolean(plot.hasPower());
        out.writeInt(plot.getBedrooms());
        out.writeInt(plot.getBeds());
        out.writeDouble(plot.getSizeM2());
        out.writeDouble(plot.getCostPerNight());
        writeString(out, plot.getPhotoUrl());
        writeStringList(out, plot.getFacilities());
    }

    private static void writeGuest(DataOutputStream out, CampsiteSystem system, Guest guest)
            throws IOException {
        out.writeInt(guest.getId());
        out.writeUTF(guest.getName());
        out.writeUTF(guest.getDateOfBirth().toString());
        writeString(out, guest.getAddress());
        writeString(out, guest.getPhone());
        writeString(out, guest.getEmail());
        writeString(out, guest.getNotes());

        out.writeInt(guest.getBookings().size());
        Node<Booking> bookingNode = guest.getBookings().getHead();
        while (bookingNode != null) {
            writeBooking(out, system, bookingNode.getData());
            bookingNode = bookingNode.getNext();
        }
    }

    private static void writeBooking(DataOutputStream out, CampsiteSystem system, Booking booking)
            throws IOException {
        out.writeInt(booking.getBookingId());
        out.writeUTF(booking.getCampsiteName());
        out.writeUTF(booking.getAreaId());
        out.writeInt(booking.getGuestId());
        out.writeUTF(booking.getStartDate().toString());
        out.writeUTF(booking.getEndDate().toString());

        BookingPreferences prefs = booking.getPreferences();
        out.writeBoolean(prefs.getType() != null);
        if (prefs.getType() != null) {
            out.writeUTF(prefs.getType().name());
        }
        out.writeInt(prefs.getNumGuests());
        out.writeBoolean(prefs.isNeedsPower());
        out.writeDouble(prefs.getMinSizeM2());
        out.writeInt(prefs.getMinBedrooms());
        out.writeDouble(prefs.getBudget());

        Plot plot = booking.getAssignedPlot();
        if (plot == null) {
            out.writeByte(PLOT_NONE);
        } else if (isStillInSystem(system, booking, plot)) {
            out.writeByte(PLOT_ATTACHED);
            out.writeUTF(plot.getIdentifier());
        } else {
            // The plot was removed after the stay finished: keep a copy so the history is complete
            out.writeByte(PLOT_DETACHED);
            writePlot(out, plot);
        }
    }

    private static boolean isStillInSystem(CampsiteSystem system, Booking booking, Plot plot) {
        Campsite campsite = system.findCampsite(booking.getCampsiteName());
        CampingArea area = (campsite == null) ? null : campsite.findArea(booking.getAreaId());
        return area != null && area.findPlot(plot.getIdentifier()) == plot;
    }

    // ------------------------------------------------------------------
    // Loading
    // ------------------------------------------------------------------

    /**
     * Loads a system from a file. A new system is built and returned, so if anything
     * goes wrong the caller's current system is left untouched.
     *
     * @throws IOException if the file is missing, not a campsite file, or corrupt
     */
    public static CampsiteSystem load(Path file) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
            if (in.readInt() != MAGIC) {
                throw new IOException("This is not a campsite data file");
            }
            int version = in.readInt();
            if (version != VERSION) {
                throw new IOException("Unsupported file version " + version);
            }

            CampsiteSystem system = new CampsiteSystem();
            int nextGuestId = in.readInt();
            int nextBookingId = in.readInt();

            int campsiteCount = readCount(in);
            for (int i = 0; i < campsiteCount; i++) {
                system.getCampsites().add(readCampsite(in));
            }
            int guestCount = readCount(in);
            for (int i = 0; i < guestCount; i++) {
                system.getGuests().add(readGuest(in, system));
            }
            system.setIdCounters(nextGuestId, nextBookingId);
            return system;
        } catch (IllegalArgumentException e) {
            // A model constructor rejected a value, so the file content is invalid
            throw new IOException("The data file is corrupt: " + e.getMessage(), e);
        }
    }

    private static Campsite readCampsite(DataInputStream in) throws IOException {
        String name = in.readUTF();
        String address = readString(in);
        int stars = in.readInt();
        String contact = readString(in);
        Campsite campsite = new Campsite(name, address, stars, contact);
        readFacilitiesInto(in, campsite.getFacilities());

        int areaCount = readCount(in);
        for (int i = 0; i < areaCount; i++) {
            CampingArea area = new CampingArea(in.readUTF(), readString(in));
            int plotCount = readCount(in);
            for (int j = 0; j < plotCount; j++) {
                area.addPlot(readPlot(in));
            }
            campsite.addArea(area);
        }
        return campsite;
    }

    private static Plot readPlot(DataInputStream in) throws IOException {
        String identifier = in.readUTF();
        PlotType type = PlotType.valueOf(in.readUTF());
        boolean hasPower = in.readBoolean();
        int bedrooms = in.readInt();
        int beds = in.readInt();
        double size = in.readDouble();
        double cost = in.readDouble();
        String photo = readString(in);
        Plot plot = new Plot(identifier, type, hasPower, bedrooms, beds, size, cost, photo);
        readFacilitiesInto(in, plot.getFacilities());
        return plot;
    }

    private static Guest readGuest(DataInputStream in, CampsiteSystem system) throws IOException {
        int id = in.readInt();
        String name = in.readUTF();
        LocalDate dateOfBirth = LocalDate.parse(in.readUTF());
        Guest guest = new Guest(id, name, dateOfBirth,
                readString(in), readString(in), readString(in), readString(in));

        int bookingCount = readCount(in);
        for (int i = 0; i < bookingCount; i++) {
            guest.addBooking(readBooking(in, system));
        }
        return guest;
    }

    private static Booking readBooking(DataInputStream in, CampsiteSystem system) throws IOException {
        int bookingId = in.readInt();
        String campsiteName = in.readUTF();
        String areaId = in.readUTF();
        int guestId = in.readInt();
        LocalDate start = LocalDate.parse(in.readUTF());
        LocalDate end = LocalDate.parse(in.readUTF());

        PlotType type = in.readBoolean() ? PlotType.valueOf(in.readUTF()) : null;
        BookingPreferences prefs = new BookingPreferences(type, in.readInt(), in.readBoolean(),
                in.readDouble(), in.readInt(), in.readDouble());
        Booking booking = new Booking(bookingId, campsiteName, areaId, guestId, start, end, prefs);

        byte mode = in.readByte();
        if (mode == PLOT_ATTACHED) {
            Plot plot = findPlot(system, campsiteName, areaId, in.readUTF());
            booking.setAssignedPlot(plot);
            plot.addBooking(booking);
        } else if (mode == PLOT_DETACHED) {
            booking.setAssignedPlot(readPlot(in));
        } else if (mode != PLOT_NONE) {
            throw new IOException("The data file is corrupt (unknown plot mode " + mode + ")");
        }
        return booking;
    }

    private static Plot findPlot(CampsiteSystem system, String campsiteName, String areaId,
                                 String plotId) throws IOException {
        Campsite campsite = system.findCampsite(campsiteName);
        CampingArea area = (campsite == null) ? null : campsite.findArea(areaId);
        Plot plot = (area == null) ? null : area.findPlot(plotId);
        if (plot == null) {
            throw new IOException("The data file is corrupt (booking refers to missing plot "
                    + plotId + ")");
        }
        return plot;
    }

    // ------------------------------------------------------------------
    // Small read/write helpers
    // ------------------------------------------------------------------

    /** Writes a string that may be null (a flag first, then the text). */
    private static void writeString(DataOutputStream out, String text) throws IOException {
        out.writeBoolean(text != null);
        if (text != null) {
            out.writeUTF(text);
        }
    }

    private static String readString(DataInputStream in) throws IOException {
        return in.readBoolean() ? in.readUTF() : null;
    }

    private static void writeStringList(DataOutputStream out, MyLinkedList<String> list)
            throws IOException {
        out.writeInt(list.size());
        Node<String> node = list.getHead();
        while (node != null) {
            out.writeUTF(node.getData());
            node = node.getNext();
        }
    }

    private static void readFacilitiesInto(DataInputStream in, MyLinkedList<String> target)
            throws IOException {
        int count = readCount(in);
        for (int i = 0; i < count; i++) {
            target.add(in.readUTF());
        }
    }

    /** Reads a count and rejects negative numbers, which only a corrupt file can contain. */
    private static int readCount(DataInputStream in) throws IOException {
        int count = in.readInt();
        if (count < 0) {
            throw new IOException("The data file is corrupt (negative count)");
        }
        return count;
    }
}
