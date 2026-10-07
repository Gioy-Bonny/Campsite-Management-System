package campsite.model;

import campsite.structures.MyLinkedList;
import campsite.structures.Node;

import java.time.LocalDate;

/**
 * A guest of the campsite system. A guest can have many bookings,
 * across many campsites, both pending and past.
 */
public class Guest {

    /**
     * @param id          unique guest number
     * @param name        full name
     * @param dateOfBirth date of birth (cannot be in the future)
     * @param address     postal address
     * @param phone       telephone number
     * @param email       email address
     * @param notes       any extra information (allergies, accessibility needs...)
     */
    private final int id;
    private final String name;
    private final LocalDate dateOfBirth;
    private final String address;
    private final String phone;
    private final String email;
    private final String notes;
    private final MyLinkedList<Booking> bookings = new MyLinkedList<>();

    public Guest(int id, String name, LocalDate dateOfBirth, String address, String phone, String email, String notes) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Guest name is required");
        }
        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth must be in the past");
        }
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.notes = notes;
    }

    /** Records a booking against this guest. */
    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    /** @return true if the booking was found and removed */
    public boolean removeBooking(Booking booking) {
        return bookings.remove(booking);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getNotes() { return notes; }
    public MyLinkedList<Booking> getBookings() { return bookings; }

    @Override
    public String toString() {
        return "Guest #" + id + ": " + name;
    }
}
