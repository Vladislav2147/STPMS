package by.bstu.svs.stpms.myrecipes;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import by.bstu.svs.stpms.myrecipes.manager.JsonManager;
import by.bstu.svs.stpms.myrecipes.model.CookingBook;
import by.bstu.svs.stpms.myrecipes.model.Recipe;
import by.bstu.svs.stpms.myrecipes.recycler.RecipeAdapter;

//TODO sorting
public class MainActivity extends AppCompatActivity {

    private static final String json = "cooking_book.json";
    private RecyclerView recipesRecyclerView;
    private RecipeAdapter recipeAdapter;
    private JsonManager jsonManager;
    private CookingBook cookingBook;
    private Intent recipeIntent;

    private List<Recipe> searchedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recipeIntent = new Intent(this, RecipeShowActivity.class);
        jsonManager = new JsonManager(new File(super.getFilesDir(), json));
        cookingBook = jsonManager.getFromFile().orElse(new CookingBook());
        searchedList = cookingBook.getRecipes();
        initRecycleView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        cookingBook = jsonManager.getFromFile().orElse(new CookingBook());
        recipeAdapter.setRecipes(cookingBook.getRecipes());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnCloseListener(() -> {
            searchedList = new ArrayList<>(cookingBook.getRecipes());
            recipeAdapter.setRecipes(searchedList);
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchedList = cookingBook
                        .getRecipes()
                        .stream()
                        .filter(recipe -> recipe.getTitle().contains(query))
                        .collect(Collectors.toList());
                recipeAdapter.setRecipes(searchedList);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) recipeAdapter.setRecipes(cookingBook.getRecipes());
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Comparator<Recipe> comparator = null;
        switch (item.getItemId()) {
            case R.id.sorting_default:
                searchedList = new ArrayList<>(cookingBook.getRecipes());
                recipeAdapter.setRecipes(searchedList);
                break;
            case R.id.sorting_by_name:
                comparator = (recipe, recipe2) -> recipe.getTitle().compareTo(recipe2.getTitle());
                break;
            case R.id.sorting_by_name_desc:
                comparator = (recipe, recipe2) -> recipe2.getTitle().compareTo(recipe.getTitle());
                break;
        }
        if (comparator != null) {
            Collections.sort(searchedList, comparator);
            recipeAdapter.setRecipes(searchedList);
        }
        return true;

    }

    public void createRecipe(View view) {
        Intent recipeCreateIntent = new Intent(this, RecipeCreateActivity.class);
        startActivity(recipeCreateIntent);
    }

    public void initRecycleView() {

        recipeAdapter = new RecipeAdapter(cookingBook.getRecipes(), super.getFilesDir());
        recipeAdapter.setOnClickListener(recipe -> {
            recipeIntent.putExtra("recipeId", recipe.getId());
            startActivity(recipeIntent);
        });
        recipeAdapter.setOnLongClickListener((recipe, view) -> {

            Context context = this;
            PopupMenu popupMenu = new PopupMenu(context, view, Gravity.END);
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

        recipesRecyclerView = findViewById(R.id.recipe_recycler_view);
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipesRecyclerView.setAdapter(recipeAdapter);

    }

    public void editItem(Long recipeId) {
        Intent recipeUpdateIntent = new Intent(this, RecipeCreateActivity.class);
        recipeUpdateIntent.putExtra("recipeId", recipeId);
        startActivity(recipeUpdateIntent);
    }

    public void deleteItem(Long recipeId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Delete")
                .setIcon(R.drawable.ic_sharp_warning_18)
                .setMessage("Delete item?")
                .setPositiveButton("Ok", (dialogInterface, i) -> {
                    CookingBook book = jsonManager.getFromFile().orElse(new CookingBook());
                    book.removeById(recipeId);
                    jsonManager.writeToFile(book);
                    recipeAdapter.setRecipes(book.getRecipes());
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();

    }
}