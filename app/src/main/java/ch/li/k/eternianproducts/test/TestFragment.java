package ch.li.k.eternianproducts.test;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.stream.Collectors;

import ch.li.k.eternianproducts.MainActivity;
import ch.li.k.eternianproducts.R;
import ch.li.k.eternianproducts.databinding.FragmentTestBinding;

public class TestFragment extends Fragment {

    public View animationContainer;
    public FrameLayout animationBarTop;
    public FrameLayout animationBarBottom;

    Uri videoUri;
    VideoView video;
    TestAdapter adapter;
    RecyclerView recyclerView;

    int bound10;
    int nElements;
    String operators;

    SharedPreferences sharedPreferences;
    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            operators = sharedPreferences.getString("preference_operators", "MULTIDIVI");
            bound10 = Integer.parseInt(sharedPreferences.getString("preference_calcRange", "12"));
            nElements = Integer.parseInt(sharedPreferences.getString("preference_nElements", "12"));
            System.out.println(nElements + ", " + bound10 + ", " + operators);

            updateModel(nElements, bound10, operators);
        }
    };

    Observer observer = new Observer() {
        @Override
        public void onChanged(@Nullable Object o) {
            boolean allCorrect = adapter.getTestModelList().getAllCorrect().getValue().stream().allMatch(isCorrect -> isCorrect);
            System.out.println("--> model list: " + adapter.getTestModelList().getAllCorrect().getValue().stream().map((v) -> v.toString()).collect(Collectors.toCollection(ArrayList::new)));
            System.out.println("--> all correct: " + allCorrect);
            if (allCorrect) {
                System.out.println("Running video...");
                runAnimationHeMan();
            }
        }
    };

    public TestFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        FragmentTestBinding binding = FragmentTestBinding.inflate(inflater, container, false);
        animationBarBottom = binding.animationBarBottom;
        animationBarTop = binding.animationBarTop;
        recyclerView = binding.recyclerTest;

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new TestAdapter();
        adapter.getTestModelList().getAllCorrect().observe(this, observer);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false); // Simple fix for flickering view

        initPreferences();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.videoUri = Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + "heman_trafo");
    }

    void initPreferences() {
        PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

        operators = sharedPreferences.getString("preference_operators", "MULTIDIVI");
        bound10 = Integer.parseInt(sharedPreferences.getString("preference_calcRange", "12"));
        nElements = Integer.parseInt(sharedPreferences.getString("preference_nElements", "12"));

        updateModel(nElements, bound10, operators);
    }

    public void updateModel() {
        adapter.testModelList.updateModelList();
        adapter.notifyDataSetChanged();
    }

    public void updateModel(int nElements, int bound10, String operators) {
        adapter.testModelList.updateModelList(nElements, bound10, operators);
        adapter.notifyDataSetChanged();
    }

    public void runAnimationHeMan() {
        adapter.getTestModelList().getAllCorrect().removeObservers(this);

//        try {
//            animationBarBottom.removeAllViews();
//        } catch (NullPointerException e) {
//        }

        View container = LayoutInflater.from(getContext())
                .inflate(R.layout.animation_heman, animationBarBottom);
        container.setVisibility(View.VISIBLE);

        video = container.findViewById(R.id.video_heman);
        video.setVideoURI(this.videoUri);
        video.seekTo(1);
        video.start();

        video.setOnCompletionListener((v) -> {
            video.stopPlayback();
            System.out.println("Video at: " + video.getCurrentPosition());
            ((MainActivity) getActivity()).getMainMenu().performIdentifierAction(R.id.action_update, 0);
        });
    }

    public void runAnimationOrko() {
        try {
            animationBarBottom.removeAllViews();
        } catch (NullPointerException e) {
        }

        View container = LayoutInflater.from(getContext())
                .inflate(R.layout.animation_orko, animationBarBottom);
        container.setVisibility(View.VISIBLE);

        TransitionManager.beginDelayedTransition(animationBarBottom);
        container.postDelayed(() -> {
            TransitionManager.beginDelayedTransition(animationBarBottom);
            container.setVisibility(View.GONE);
        }, 3000);

        adapter.getTestModelList().getAllCorrect().observe(this, observer);
    }

    public void runAnimationBeastMan() {
//        try {
//            animationBarBottom.removeAllViews();
//        } catch (NullPointerException e) {
//        }

        View container = LayoutInflater.from(getContext())
                .inflate(R.layout.animation_game_over, animationBarBottom);
        container.setVisibility(View.GONE);

        TransitionManager.beginDelayedTransition(animationBarBottom);
        container.postDelayed(() -> {
            TransitionManager.beginDelayedTransition(animationBarBottom);
            container.setVisibility(View.VISIBLE);
        }, 3000);

        MediaPlayer player = MediaPlayer.create(getContext(), R.raw.skeletor_laugh);
        player.start();
    }

    public void runAnimationSkeletor(long tick, float timeout) {
        animationContainer = LayoutInflater.from(getContext())
                .inflate(R.layout.animation_skeletor, animationBarTop);
        animationContainer.setAlpha((float) ((timeout - tick) / timeout));
    }
}