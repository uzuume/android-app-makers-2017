package fr.paug.androidmakers.ui.fragment;

import android.net.Uri;

import fr.paug.androidmakers.manager.AgendaRepository;
import fr.paug.androidmakers.model.Venue;

public class VenueConferenceFragment extends AbstractVenueFragment {

    private final String CONFERENCE_VENUE_COORDINATES_URI =
            "geo:" + getVenueInformations().getCoordinates() + "?q=" + Uri.encode("" + getVenueInformations().getName());

    protected Uri getVenueCoordinatesUri() {
        return Uri.parse(CONFERENCE_VENUE_COORDINATES_URI);
    }

    @Override
    protected Venue getVenueInformations() {
        return AgendaRepository.getInstance().getVenue(1);
    }

}