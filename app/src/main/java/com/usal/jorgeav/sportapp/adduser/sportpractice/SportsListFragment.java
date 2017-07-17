package com.usal.jorgeav.sportapp.adduser.sportpractice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.usal.jorgeav.sportapp.BaseFragment;
import com.usal.jorgeav.sportapp.R;
import com.usal.jorgeav.sportapp.adapters.AddSportsAdapter;
import com.usal.jorgeav.sportapp.data.Sport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SportsListFragment extends BaseFragment {
    private final static String TAG = SportsListFragment.class.getSimpleName();
    public static final String BUNDLE_INSTANCE_SPORT_LIST = "BUNDLE_INSTANCE_SPORT_LIST";

    @BindView(R.id.recycler_list)
    RecyclerView sportsRecyclerViewList;
    private AddSportsAdapter mSportAdapter;

    public SportsListFragment() {
        // Required empty public constructor
    }

    public static SportsListFragment newInstance(ArrayList<Sport> sportsList) {
        SportsListFragment fragment = new SportsListFragment();
        if (sportsList != null) {
            Bundle args = new Bundle();
            args.putParcelableArrayList(BUNDLE_INSTANCE_SPORT_LIST, sportsList);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_ok, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.action_ok) {
            Log.d(TAG, "onOptionsItemSelected: Ok");
            if (getActivity() instanceof OnSportsSelected) {
                ((OnSportsSelected) getActivity()).retrieveSportsSelected(mSportAdapter.getDataAsArrayList());
                getActivity().onBackPressed();
            } else {
                Log.e(TAG, "onOptionsItemSelected: Activity does not implement OnSportsSelected");
            }
            return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, root);

        mSportAdapter = new AddSportsAdapter(null);
        sportsRecyclerViewList.setAdapter(mSportAdapter);
        sportsRecyclerViewList.setHasFixedSize(true);
        sportsRecyclerViewList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        return root;
    }

    private List<Sport> loadSports() {
        ArrayList<Sport> result = new ArrayList<>();

        String[] sportsNameArray = getResources().getStringArray(R.array.sport_id);
        for (String aSportsNameArray : sportsNameArray) {
            result.add(new Sport(aSportsNameArray, 0f, 0));
        }

        if (getArguments() != null && getArguments().containsKey(BUNDLE_INSTANCE_SPORT_LIST)) {
            ArrayList<Sport> sportsListFromActivity = getArguments().getParcelableArrayList(BUNDLE_INSTANCE_SPORT_LIST);
            if (sportsListFromActivity != null) {
                for (Sport sportFromActivity : sportsListFromActivity)
                    for (Sport sportFromResources : result)
                        if (isTheSameSport(sportFromActivity, sportFromResources)) {
                            sportFromResources.setPunctuation(sportFromActivity.getPunctuation());
                            break;
                        }
            }
        }

        return result;
    }

    private boolean isTheSameSport(Sport a, Sport b) {
        return a.getmName().equals(b.getmName());
    }

    @Override
    public void onStart() {
        super.onStart();
        hideSoftKeyboard();
        mFragmentManagementListener.setCurrentDisplayedFragment("Selecciona deportes", this);
        if (mActionBarIconManagementListener != null) mActionBarIconManagementListener.setToolbarAsUp();
        mSportAdapter.replaceData(loadSports());
        showContent();
    }

    @Override
    public void onPause() {
        super.onPause();
        mFragmentManagementListener.setCurrentDisplayedFragment(null, null);
        mSportAdapter.replaceData(null);
    }

    public interface OnSportsSelected {
        void retrieveSportsSelected(List<Sport> sportsSelected);
    }
}
