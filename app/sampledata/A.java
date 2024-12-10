<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="16dp"
    android:background="@drawable/rounded_card"
    android:gravity="center_vertical"
    android:layout_marginBottom="8dp">

    <!-- Task Name -->
    <TextView
        android:id="@+id/task_name"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Task Name"
        android:textSize="16sp"
        android:textColor="#000000" />

    <!-- Priority Badge -->
    <TextView
        android:id="@+id/task_priority"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:padding="8dp"
        android:text="Priority"
        android:textColor="#FFFFFF"
        android:background="@drawable/priority_badge_background"
        android:textSize="12sp" />
</LinearLayout>
