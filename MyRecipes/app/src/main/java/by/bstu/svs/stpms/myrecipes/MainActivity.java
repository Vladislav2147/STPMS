package by.bstu.svs.stpms.myrecipes;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import by.bstu.svs.stpms.myrecipes.manager.DatabaseContract;
import by.bstu.svs.stpms.myrecipes.manager.DatabaseRecipeManager;
import by.bstu.svs.stpms.myrecipes.manager.exception.SQLiteDatabaseException;
import by.bstu.svs.stpms.myrecipes.model.Query;

public class MainActivity extends BaseActivity {

    private FrameLayout listContainer;
    private FrameLayout detailsContainer;
    private ProgressBar progressBar;
    private LinearLayout ll_content;


    private RecipeFragment recipeListFragment;
    private RecipeDetailsFragment recipeDetailsFragment;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listContainer = findViewById(R.id.recipe_container);
        detailsContainer = findViewById(R.id.recipe_details_container);
        progressBar = findViewById(R.id.loading);
        ll_content = findViewById(R.id.container);

        fragmentManager = getSupportFragmentManager();

        int orientation = getResources().getConfiguration().orientation;
        initShowingDetails(orientation);

        initDrawer();

    }

    private void initDrawer() {
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.recipe_nav:
                    break;
                case R.id.setting_nav:
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        });

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.app_name,
                R.string.app_name
        );

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnCloseListener(() -> {
            new SQLiteAsyncTask().execute(new Query());
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                queryText = "%" + queryText + "%";
                new SQLiteAsyncTask().execute(new Query(
                        "title like ?",
                        new String[] {queryText},
                        null
                ));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) {
                    Cursor cursor = DatabaseRecipeManager.getInstance(MainActivity.this).getCursorByQuery(new Query());
                    recipeListFragment.updateAdapterByCursor(cursor);
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Query query = null;

        switch (item.getItemId()) {
            case R.id.scroll_up:
                recipeListFragment.getRecipesRecyclerView().smoothScrollToPosition(0);
                break;
            case R.id.sorting_default:
                query = new Query();
                break;
            case R.id.sorting_by_name:
                query = new Query(
                        null,
                        null,
                        DatabaseContract.RecipeTable.COLUMN_NAME_TITLE + " COLLATE NOCASE"
                );
                break;
            case R.id.sorting_by_category:
               query = new Query(
                        null,
                        null,
                        DatabaseContract.RecipeTable.COLUMN_NAME_CATEGORY + " COLLATE NOCASE"
                );
                break;
            case R.id.favorite:
                query = new Query(
                        "is_favorite == 1",
                        null,
                        null
                );
                break;
        }

        if (query != null) {
            new SQLiteAsyncTask().execute(query);
        }

        return true;

    }

    public void createRecipe(View view) {
        Intent recipeCreateIntent = new Intent(this, RecipeCreateActivity.class);
        startActivity(recipeCreateIntent);
    }

    private void initShowingDetails(int orientation) {

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            recipeListFragment = RecipeFragment.newInstance(1, R.layout.fragment_recipe);

            detailsContainer.setVisibility(View.VISIBLE);

            recipeListFragment.setOnClickListener(recipe -> {
                recipeDetailsFragment = RecipeDetailsFragment.newInstance(recipe.getId());

                fragmentManager
                        .beginTransaction()
                        .replace(R.id.recipe_details_container, recipeDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            });

        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // In portrait
            recipeListFragment = RecipeFragment.newInstance(2, R.layout.fragment_recipe_2);

            detailsContainer.setVisibility(View.GONE);
            recipeListFragment.setOnClickListener(recipe -> {
                Intent recipeIntent = new Intent(this, RecipeShowActivity.class);
                recipeIntent.putExtra("recipeId", recipe.getId());
                startActivity(recipeIntent);
            });
        }


        fragmentManager
                .beginTransaction()
                .replace(R.id.recipe_container, recipeListFragment)
                .commit();

        recipeListFragment.setOnLongClickListener((recipe, view) -> {

            PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END);
            popupMenu.inflate(R.menu.recipe_popup_menu);
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.edit_item:
                        editItem(recipe.getId());
                        break;
                    case R.id.delete_item:
                        deleteItem(recipe.getId());
                        break;
                }
                return true;
            });
            popupMenu.show();
            return true;
        });
    }

    public void editItem(Integer recipeId) {
        Intent recipeUpdateIntent = new Intent(this, RecipeCreateActivity.class);
        recipeUpdateIntent.putExtra("recipeId", recipeId);
        startActivity(recipeUpdateIntent);
    }

    public void deleteItem(Integer recipeId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Delete")
                .setIcon(R.drawable.ic_sharp_warning_18)
                .setMessage("Delete item?")
                .setPositiveButton("Ok", (dialogInterface, i) -> {
                    String message = "Recipe deleted successfully";
                    try {
                        DatabaseRecipeManager.getInstance(this).delete(recipeId);
                        new SQLiteAsyncTask().execute(new Query());
                    } catch (SQLiteDatabaseException e) {
                        message = e.getMessage();
                    }
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();


    }

    @Override
    protected void onStart() {
        super.onStart();
        new SQLiteAsyncTask().execute(new Query());
    }

    public class SQLiteAsyncTask extends AsyncTask<Query, Void, Cursor> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_content.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Cursor doInBackground(Query... queries) {
            return DatabaseRecipeManager.getInstance(MainActivity.this).getCursorByQuery(queries[0]);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            ll_content.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            recipeListFragment.updateAdapterByCursor(cursor);
        }
    }

}