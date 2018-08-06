/**
 * Created by Milan Cejnar on 01.12.2017.
 * Asamm Software, s.r.o.
 */
package com.asamm.locus.addon.wear.gui.trackrec.stats.view;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.asamm.locus.addon.wear.R;
import com.asamm.locus.addon.wear.common.communication.containers.trackrecording.TrackRecordingValue;
import com.asamm.locus.addon.wear.gui.custom.SpannableTextUtils;
import com.asamm.locus.addon.wear.gui.custom.TrackStatConsumable;
import com.asamm.locus.addon.wear.gui.trackrec.stats.model.TrackStatTypeEnum;
import com.asamm.locus.addon.wear.gui.trackrec.stats.model.TrackStatViewId;


/**
 * Componend for displaying various(specified by {@code mType} single-value statistics
 */
public class TrackStatLayout extends ConstraintLayout {

    /**
     * Type of displayed statistics
     */
    private TrackStatTypeEnum mType;
    private int mGravity;
    // formatted text of measured value/statistics
    private TextView mTextViewValue;

    private ImageView mImageViewIcon;
    private TextView mTextViewDescription;
    private TrackStatViewId trackStatViewPositionId = new TrackStatViewId(-1, -1);

    public TrackStatLayout(Context context) {
        this(context, null);
    }

    public TrackStatLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TrackStatLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mType = TrackStatTypeEnum.BLANK;
        initView(context, attrs);
    }

    private void initView(Context ctx, AttributeSet attrs) {
        // get parameters from attributes
        final TypedArray ta = ctx.obtainStyledAttributes(attrs, R.styleable.TrackStatLayout);
        boolean isPositionTopScreen = ta.getBoolean(R.styleable.TrackStatLayout_positionTop, true);
        mGravity = ta.getInteger(R.styleable.TrackStatLayout_android_gravity, Gravity.CENTER);
        ta.recycle();
        this.setOnLongClickListener(v -> {
            Intent i = new Intent(ctx, TrackStatsSelectListActivity.class);
            i.putExtra(TrackStatsSelectListActivity.PARAM_STAT_ID, mType.getId());
            i.putExtra(TrackStatsSelectListActivity.PARAM_SCREEN_IDX, trackStatViewPositionId.getScreenIdx());
            i.putExtra(TrackStatsSelectListActivity.PARAM_CELL_IDX, trackStatViewPositionId.getCellIdx());
            ((Activity) ctx).startActivityForResult(i, TrackStatsSelectListActivity.REQUEST_CODE_STATS_SELECT_LIST_ACTIVITY);
            return true;
        });

        boolean isPositionCentered = mGravity == Gravity.CENTER;
        final int layoutId;
        if (isPositionCentered && isPositionTopScreen) {
            layoutId = R.layout.track_stat_layout_icon_centered_top;
        } else if (isPositionCentered) {
            layoutId = R.layout.track_stat_layout_icon_centered_bottom;
        } else if (isPositionTopScreen) {
            layoutId = R.layout.track_stat_layout_icon_top;
        } else {
            layoutId = R.layout.track_stat_layout_icon_bottom;
        }
        View v = View.inflate(ctx,
                layoutId,
                this);
        mTextViewValue = v.findViewById(R.id.stat_value);
        mImageViewIcon = v.findViewById(R.id.stat_icon);
        mTextViewDescription = v.findViewById(R.id.stat_description);

        boolean alightRight = (mGravity & Gravity.LEFT) != Gravity.LEFT;
        mTextViewValue.setGravity(mGravity);
        mTextViewDescription.setGravity(mGravity);
        mImageViewIcon.setScaleType(isPositionCentered ? ImageView.ScaleType.FIT_CENTER :
                alightRight ? ImageView.ScaleType.FIT_END : ImageView.ScaleType.FIT_START);
        setType(mType);
    }

    /**
     * used during controller initialization to setup screen and cell id that identifies this view
     */
    public void setTrackStatViewPositionId(int screenIdx, int cellIdx) {
        this.trackStatViewPositionId = new TrackStatViewId(screenIdx, cellIdx);
    }

    public void setType(TrackStatTypeEnum statType) {
        this.mType = statType;
        mImageViewIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), mType.getIconId()));
        mTextViewDescription.setText(getResources().getText(mType.getNameStringId()));
        mTextViewValue.setText("");
    }

    public void consumeNewStatistics(TrackRecordingValue trv) {
        TrackStatConsumable.ValueUnitContainer newValue = mType.consumeAndFormat(trv);
        SpannableStringBuilder ssb = new SpannableStringBuilder(newValue.getValue());
        SpannableTextUtils.addStyledText(ssb, " " + newValue.getUnits(), 0.5f, Typeface.NORMAL, 0);
        mTextViewValue.setText(ssb);
    }

    public void setAmbientMode(boolean enabled) {
        mTextViewDescription.setTextColor(enabled ? getContext().getColor(R.color.base_light_primary) : getContext().getColor(R.color.base_dark_primary));
        mTextViewValue.setTextColor(enabled ? getContext().getColor(R.color.base_light_primary) : getContext().getColor(R.color.base_dark_primary));
        if (enabled) {
            mImageViewIcon.setColorFilter(R.color.base_light_primary);
        } else {
            mImageViewIcon.clearColorFilter();
        }
    }
}
