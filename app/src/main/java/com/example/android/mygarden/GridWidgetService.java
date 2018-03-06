package com.example.android.mygarden;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.PlantDetailActivity;
import com.example.android.mygarden.utils.PlantUtils;

/**
 * Created by avigagel on 06/03/2018.
 */

public class GridWidgetService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }

    class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {


        Context mContext;
        Cursor mCursor;


        public GridRemoteViewsFactory(Context applicationContext) {
            this.mContext = applicationContext;
        }

        @Override
        public void onCreate() {

        }

        // This method is called on Start and when notifyAppWidgetViewDataChanged is called
        @Override
        public void onDataSetChanged() {
            // Get all plant info ordered by creation time
            Uri PLANT_URI = PlantContract.BASE_CONTENT_URI.buildUpon().appendPath(PlantContract.PATH_PLANTS).build();
            if (mCursor != null) mCursor.close();
            mCursor = mContext.getContentResolver().query(
                    PLANT_URI,
                    null,
                    null,
                    null,
                    PlantContract.PlantEntry.COLUMN_CREATION_TIME
            );
            Toast.makeText(mContext, "got cursor. size:" + mCursor.getCount(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDestroy() {
            mCursor.close();
        }

        @Override
        public int getCount() {
            if (mCursor == null) return 0;
            return mCursor.getCount();
        }

        /**
         * this method acts like the onBindViewHolder method in an adapter
         *
         * @param position the current position of the item in the gridView
         * @return the RemoteViews object to display for the provided position
         */

        @Override
        public RemoteViews getViewAt(int position) {
            if (mCursor == null || mCursor.getCount() == 0) return null;

            mCursor.moveToPosition(position);
            int idIndex = mCursor.getColumnIndex(PlantContract.PlantEntry._ID);
            int createTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
            int waterTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
            int plantTypeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);

            long plantId = mCursor.getLong(idIndex);
            int plantType = mCursor.getInt(plantTypeIndex);
            long createdAt = mCursor.getLong(createTimeIndex);
            long wateredAt = mCursor.getLong(waterTimeIndex);
            long timeNow = System.currentTimeMillis();

            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.plant_widget);

            // Update the plant image
            int imgRes = PlantUtils.getPlantImageRes(mContext, timeNow - createdAt, timeNow - wateredAt, plantType);
            views.setImageViewResource(R.id.widget_plant_image, imgRes);
            views.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));

            // Always hide the water drop in GridView mode
            views.setViewVisibility(R.id.widget_water_button, View.GONE);

            // Fill in the onClick PendingIntent Template for clicking on single images in widget
            Bundle extras = new Bundle();
            extras.putLong(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            views.setOnClickFillInIntent(R.id.widget_plant_image, fillInIntent);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
