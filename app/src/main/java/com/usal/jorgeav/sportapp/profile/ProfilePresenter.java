package com.usal.jorgeav.sportapp.profile;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usal.jorgeav.sportapp.data.User;
import com.usal.jorgeav.sportapp.data.provider.SportteamContract;

/**
 * Created by Jorge Avila on 23/04/2017.
 */

public class ProfilePresenter implements ProfileContract.Presenter, LoaderManager.LoaderCallbacks<Cursor> {
    private ProfileContract.View mUserView;
    private User mUser;

    public ProfilePresenter(ProfileContract.View userView) {
        mUserView = userView;
    }

    @Override
    public void loadUser() {
//        FirebaseDatabaseActions.loadMyProfile(mUserView.getActivityContext());
    }

    @Override
    public LoaderManager.LoaderCallbacks<Cursor> getLoaderInstance() {
        return this;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //TODO tiene que desconectarse cuando se hace un cierre de sesion pq fuser.getuid() cambia pero el Cursor no lo sabe
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (fuser != null)
            switch (id) {
                case ProfileFragment.LOADER_MYPROFILE_ID:
                    return new CursorLoader(
                            this.mUserView.getActivityContext(),
                            SportteamContract.UserEntry.CONTENT_USER_URI,
                            SportteamContract.UserEntry.USER_COLUMNS,
                            SportteamContract.UserEntry.USER_ID + " = ?",
                            new String[]{args.getString(ProfileFragment.BUNDLE_INSTANCE_UID)},
                            null);
                case ProfileFragment.LOADER_MYPROFILE_SPORTS_ID:
                    return new CursorLoader(
                            this.mUserView.getActivityContext(),
                            SportteamContract.UserSportEntry.CONTENT_USER_SPORT_URI,
                            SportteamContract.UserSportEntry.USER_SPORT_COLUMNS,
                            SportteamContract.UserSportEntry.USER_ID + " = ?",
                            new String[]{args.getString(ProfileFragment.BUNDLE_INSTANCE_UID)},
                            null);
            }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ProfileFragment.LOADER_MYPROFILE_ID:
                showUser(data);
                break;
            case ProfileFragment.LOADER_MYPROFILE_SPORTS_ID:
                if(data != null && data.moveToFirst()) //Todos los usuarios tienen al menos un deporte
                    mUserView.showSports(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ProfileFragment.LOADER_MYPROFILE_ID:
                showUser(null);
                break;
            case ProfileFragment.LOADER_MYPROFILE_SPORTS_ID:
                mUserView.showSports(null);
                break;
        }
    }

    private void showUser(Cursor data) {
        if(data != null && data.moveToFirst()) {
            String photoUrl = data.getString(SportteamContract.UserEntry.COLUMN_PHOTO);
            String name = data.getString(SportteamContract.UserEntry.COLUMN_NAME);
            String city = data.getString(SportteamContract.UserEntry.COLUMN_CITY);
            String ageStr = data.getString(SportteamContract.UserEntry.COLUMN_AGE);
            int age = Integer.valueOf(ageStr);
            mUserView.showUserImage(photoUrl);
            mUserView.showUserName(name);
            mUserView.showUserCity(city);
            mUserView.showUserAge(age);
        } else {
            mUserView.showUserImage("");
            mUserView.showUserName("");
            mUserView.showUserCity("");
            mUserView.showUserAge(-1);
        }
    }

    private User cursorToUser(Cursor data) {
        if(data != null && data.moveToFirst()) {
            String id = data.getString(SportteamContract.UserEntry.COLUMN_USER_ID);
            String email = data.getString(SportteamContract.UserEntry.COLUMN_EMAIL);
            String name = data.getString(SportteamContract.UserEntry.COLUMN_NAME);
            String city = data.getString(SportteamContract.UserEntry.COLUMN_CITY);
            String ageStr = data.getString(SportteamContract.UserEntry.COLUMN_AGE);
            int age = Integer.valueOf(ageStr);
            String photoUrl = data.getString(SportteamContract.UserEntry.COLUMN_PHOTO);
            return new User(id, email, name, city, age, photoUrl, null);
        }
        return null;
    }
}
