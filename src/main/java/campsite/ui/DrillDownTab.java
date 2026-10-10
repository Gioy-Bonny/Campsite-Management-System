package campsite.ui;

import campsite.model.Booking;
import campsite.model.CampingArea;
import campsite.model.Campsite;
import campsite.model.Guest;
import campsite.model.Plot;
import campsite.service.CampsiteSystem;
import campsite.service.RemovalResult;
import campsite.service.TextUtil;
import campsite.structures.Node;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Interactive drill-down: a tree of campsites, camping areas, plots and bookings.
 * Selecting an item shows its details on the right (with the photo for a plot).
 * The tree is built from our own linked lists each time {@link #refresh()} is called;
 * it only displays the data and never stores it.
 */
public class DrillDownTab extends SplitPane implements Refreshable {

    private final AppContext ctx;
    private final TreeView<Object> tree = new TreeView<>();
    private final TextArea details = UiUtil.outputArea();
    private final ImageView photoView = new ImageView();
    private final Button removePlotButton = new Button("Remove selected plot");
    private final Button cancelBookingButton = new Button("Cancel selected booking");

    public DrillDownTab(AppContext ctx) {
        this.ctx = ctx;

        details.setWrapText(true);
        photoView.setFitWidth(320);
        photoView.setPreserveRatio(true);
        removePlotButton.setDisable(true);
        cancelBookingButton.setDisable(true);

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refresh());
        removePlotButton.setOnAction(event -> UiUtil.guard(this::removeSelectedPlot));
        cancelBookingButton.setOnAction(event -> UiUtil.guard(this::cancelSelectedBooking));

        HBox buttons = new HBox(8);
        buttons.getChildren().add(refreshButton);
        buttons.getChildren().add(removePlotButton);
        buttons.getChildren().add(cancelBookingButton);

        VBox left = new VBox(8);
        left.setPadding(new Insets(10));
        left.getChildren().add(tree);
        left.getChildren().add(buttons);
        VBox.setVgrow(tree, Priority.ALWAYS);

        VBox right = new VBox(8);
        right.setPadding(new Insets(10));
        right.getChildren().add(details);
        right.getChildren().add(photoView);
        VBox.setVgrow(details, Priority.ALWAYS);

        getItems().add(left);
        getItems().add(right);
        setDividerPositions(0.45);

        tree.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldItem, newItem) -> showDetails(newItem));
        refresh();
    }

    @Override
    public void refresh() {
        CampsiteSystem system = ctx.getSystem();
        TreeItem<Object> root = new TreeItem<>("All campsites");
        root.setExpanded(true);

        Node<Campsite> campsiteNode = system.getCampsites().getHead();
        while (campsiteNode != null) {
            TreeItem<Object> campsiteItem = new TreeItem<>(campsiteNode.getData());
            Node<CampingArea> areaNode = campsiteNode.getData().getAreas().getHead();
            while (areaNode != null) {
                TreeItem<Object> areaItem = new TreeItem<>(areaNode.getData());
                Node<Plot> plotNode = areaNode.getData().getPlots().getHead();
                while (plotNode != null) {
                    TreeItem<Object> plotItem = new TreeItem<>(plotNode.getData());
                    Node<Booking> bookingNode = plotNode.getData().getBookings().getHead();
                    while (bookingNode != null) {
                        plotItem.getChildren().add(new TreeItem<Object>(bookingNode.getData()));
                        bookingNode = bookingNode.getNext();
                    }
                    areaItem.getChildren().add(plotItem);
                    plotNode = plotNode.getNext();
                }
                campsiteItem.getChildren().add(areaItem);
                areaNode = areaNode.getNext();
            }
            root.getChildren().add(campsiteItem);
            campsiteNode = campsiteNode.getNext();
        }

        tree.setRoot(root);
        showDetails(null);
    }

    // ------------------------------------------------------------------
    // Showing details
    // ------------------------------------------------------------------

    private void showDetails(TreeItem<Object> item) {
        photoView.setImage(null);
        Object value = (item == null) ? null : item.getValue();
        removePlotButton.setDisable(!(value instanceof Plot));
        cancelBookingButton.setDisable(!(value instanceof Booking));

        if (value instanceof Campsite) {
            details.setText(describeCampsite((Campsite) value));
        } else if (value instanceof CampingArea) {
            details.setText(describeArea((CampingArea) value));
        } else if (value instanceof Plot) {
            details.setText(describePlot((Plot) value));
            showPhoto(((Plot) value).getPhotoUrl());
        } else if (value instanceof Booking) {
            details.setText(describeBooking((Booking) value));
        } else {
            details.setText("Campsites: " + ctx.getSystem().getCampsites().size()
                    + "\nGuests: " + ctx.getSystem().getGuests().size()
                    + "\n\nOpen the tree on the left and select a campsite, camping area, plot or"
                    + " booking to see its details.");
        }
    }

    private String describeCampsite(Campsite campsite) {
        return "CAMPSITE\n"
                + "Name: " + campsite.getName() + "\n"
                + "Address: " + TextUtil.orDash(campsite.getAddress()) + "\n"
                + "Stars: " + campsite.getStars() + "\n"
                + "Contact: " + TextUtil.orDash(campsite.getContactInfo()) + "\n"
                + "Facilities: " + TextUtil.join(campsite.getFacilities()) + "\n"
                + "Camping areas: " + campsite.getAreas().size();
    }

    private String describeArea(CampingArea area) {
        return "CAMPING AREA\n"
                + "Identifier: " + area.getIdentifier() + "\n"
                + "Description: " + TextUtil.orDash(area.getDescription()) + "\n"
                + "Plots: " + area.getPlots().size();
    }

    private String describePlot(Plot plot) {
        return "PLOT / PITCH\n"
                + "Identifier: " + plot.getIdentifier() + "\n"
                + "Type: " + plot.getType() + "\n"
                + "Power: " + (plot.hasPower() ? "yes" : "no") + "\n"
                + "Bedrooms: " + plot.getBedrooms() + ", beds: " + plot.getBeds() + "\n"
                + "Size: " + plot.getSizeM2() + " m2\n"
                + "Cost per night: " + TextUtil.euro(plot.getCostPerNight()) + "\n"
                + "Facilities: " + TextUtil.join(plot.getFacilities()) + "\n"
                + "Photo URL: " + TextUtil.orDash(plot.getPhotoUrl()) + "\n"
                + "Bookings on this plot: " + plot.getBookings().size();
    }

    private String describeBooking(Booking booking) {
        Guest guest = ctx.getSystem().findGuest(booking.getGuestId());
        String guestName = (guest == null) ? "unknown" : guest.getName();
        boolean past = booking.isPast(ctx.today());
        return "BOOKING #" + booking.getBookingId() + (past ? " (past)" : " (pending)") + "\n"
                + "Guest: " + booking.getGuestId() + " " + guestName + "\n"
                + "Campsite / area: " + booking.getCampsiteName() + " / " + booking.getAreaId() + "\n"
                + "Plot: " + (booking.getAssignedPlot() == null
                        ? "unassigned" : booking.getAssignedPlot().getIdentifier()) + "\n"
                + "Dates: " + booking.getStartDate() + " to " + booking.getEndDate()
                + " (" + booking.getNights() + " nights)\n"
                + "Total cost: " + TextUtil.euro(booking.getTotalCost()) + "\n"
                + "Preferences: " + booking.getPreferences();
    }

    private void showPhoto(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            photoView.setImage(new Image(url.trim(), true)); // true = load in the background
        } catch (IllegalArgumentException e) {
            // Not a usable URL, so no photo is shown; the URL is still listed in the details
        }
    }

    // ------------------------------------------------------------------
    // Actions on the selected item
    // ------------------------------------------------------------------

    private void removeSelectedPlot() {
        TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
        if (item == null || !(item.getValue() instanceof Plot)) {
            throw new IllegalArgumentException("Select a plot in the tree first.");
        }
        Plot plot = (Plot) item.getValue();
        CampingArea area = (CampingArea) item.getParent().getValue();
        Campsite campsite = (Campsite) item.getParent().getParent().getValue();

        if (!UiUtil.confirm("Remove plot " + plot.getIdentifier() + " from " + area.getIdentifier()
                + "?\nIts pending bookings are moved to another suitable plot of "
                + campsite.getName() + " where possible.")) {
            return;
        }
        RemovalResult result = ctx.getSystem().removePlot(
                campsite.getName(), area.getIdentifier(), plot.getIdentifier(), ctx.today());
        ctx.changed();
        refresh();
        UiUtil.showInfo("Plot removed: " + result);
    }

    private void cancelSelectedBooking() {
        TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
        if (item == null || !(item.getValue() instanceof Booking)) {
            throw new IllegalArgumentException("Select a booking in the tree first.");
        }
        Booking booking = (Booking) item.getValue();
        if (!UiUtil.confirm("Cancel booking #" + booking.getBookingId() + "?")) {
            return;
        }
        if (ctx.getSystem().cancelBooking(booking.getBookingId(), ctx.today())) {
            ctx.changed();
            refresh();
            UiUtil.showInfo("Booking #" + booking.getBookingId() + " was cancelled.");
        } else {
            UiUtil.showError("Booking #" + booking.getBookingId()
                    + " could not be cancelled. Bookings that have already finished are kept as history.");
        }
    }
}
