package ru.euphoria.doggy.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

import ru.euphoria.doggy.R;

public class MiniPlayer extends FrameLayout implements CoordinatorLayout.AttachedBehavior {
    public ProgressBar progress;
    public TextView title;
    public TextView artist;
    public ImageView overflow;
    public ImageView play;

    public MiniPlayer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MiniPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MiniPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View player = View.inflate(context, R.layout.mini_player, null);
        title = player.findViewById(R.id.audio_title);
        artist = player.findViewById(R.id.audio_artist);
        progress = player.findViewById(R.id.audio_progress);
        overflow = player.findViewById(R.id.overflow);
        play = player.findViewById(R.id.play);

        addView(player);
    }

    @NonNull
    @Override
    public CoordinatorLayout.Behavior getBehavior() {
        return new Behavior();
    }

    public static class Behavior extends CoordinatorLayout.Behavior<MiniPlayer> {
        @Override
        public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull MiniPlayer child, @NonNull View dependency) {
            return dependency instanceof Snackbar.SnackbarLayout;
        }

        @Override
        public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent,
                                              @NonNull MiniPlayer child,
                                              @NonNull View dependency) {
            if (dependency instanceof Snackbar.SnackbarLayout) {
                child.setTranslationY(dependency.getTranslationY() - dependency.getHeight());

                return true;
            }
            return false;
        }
    }
}
