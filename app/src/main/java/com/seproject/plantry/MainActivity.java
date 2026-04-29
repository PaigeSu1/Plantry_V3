package com.seproject.plantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.navigation.NavigationView;
import com.seproject.plantry.database.PantryDatabase;
import com.seproject.plantry.database.PantryItem;
import com.seproject.plantry.databinding.ActivityMainBinding;
import com.seproject.plantry.utils.PantryViewModel;

import java.util.prefs.Preferences;

/// The main activity. All of the business logic should go in here
public class MainActivity extends AppCompatActivity {
    // private PantryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // viewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        setTheme(getApplicationContext());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView navbar = findViewById(R.id.nav_toolbar);
        NavigationUI.setupWithNavController(navbar, navController);

        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            if (destination.getId() == R.id.pantryGroupFragment) {
                if (args != null) {
                    String groupName = args.getString("groupName");
                    destination.setLabel(groupName != null ? groupName : "Item Name");
                }
            }
        });
    }

    /// Sets the theme of the app (ensures that it is actually set on start up!)
    private void setTheme(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        switch (preferences.getString("theme_selection", "system")) {
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }
}