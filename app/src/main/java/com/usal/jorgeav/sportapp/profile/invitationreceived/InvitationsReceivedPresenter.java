package com.usal.jorgeav.sportapp.profile.invitationreceived;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.usal.jorgeav.sportapp.data.provider.SportteamLoader;
import com.usal.jorgeav.sportapp.network.firebase.sync.FirebaseSync;
import com.usal.jorgeav.sportapp.utils.Utiles;

class InvitationsReceivedPresenter implements InvitationsReceivedContract.Presenter, LoaderManager.LoaderCallbacks<Cursor> {
    @SuppressWarnings("unused")
    private static final String TAG = InvitationsReceivedPresenter.class.getSimpleName();

    private InvitationsReceivedContract.View mInvitationsReceivedView;

    InvitationsReceivedPresenter(InvitationsReceivedContract.View mEventInvitationsView) {
        this.mInvitationsReceivedView = mEventInvitationsView;
    }

    @Override
    public void loadEventInvitations(LoaderManager loaderManager, Bundle b) {
        FirebaseSync.loadEventsFromInvitationsReceived();
        loaderManager.initLoader(SportteamLoader.LOADER_EVENT_INVITATIONS_RECEIVED_ID, b, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String currentUserID = Utiles.getCurrentUserId();
        if (TextUtils.isEmpty(currentUserID)) return null;
        switch (id) {
            case SportteamLoader.LOADER_EVENT_INVITATIONS_RECEIVED_ID:
                return SportteamLoader
                        .cursorLoaderEventsForEventInvitationsReceived(mInvitationsReceivedView.getActivityContext(), currentUserID);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mInvitationsReceivedView.showEventInvitations(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mInvitationsReceivedView.showEventInvitations(null);
    }

}