package com.piyush.imagesearch.ux;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.piyush.imagesearch.R;
import com.piyush.imagesearch.dataObj.ImageObj;
import com.piyush.imagesearch.utils.AppController;
import com.piyush.imagesearch.utils.CONST;
import com.piyush.imagesearch.utils.PaginationScrollListener;
import com.piyush.imagesearch.views.CustomSnackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int PAGE_START = 1;
    LinearLayout main_layout;
    Toolbar toolbar;
    ImageView search;
    EditText search_keyword;
    RecyclerView image_recycler;
    ProgressBar progressBar;
    TextView info_text;
    LinearLayout info_layout;

    String search_keyword_text;
    GridLayoutManager GridLayoutManager;
    RecyclerAdapter adapter;
    ArrayList<ImageObj> list = new ArrayList<>();
    String url;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = PAGE_START;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getIds();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.requestFocus();

        try {
            SharedPreferences sharedpreferences = getSharedPreferences("firstRun", MODE_PRIVATE);
            Boolean first_run = sharedpreferences.getBoolean("first_run", true);

            if (first_run){
                info_layout.setVisibility(View.VISIBLE);
            }else {
                info_layout.setVisibility(View.GONE);
            }
        }catch (Exception e){
            info_layout.setVisibility(View.VISIBLE);
            e.printStackTrace();
        }

        SharedPreferences prefs = getSharedPreferences("firstRun", MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("first_run", false);
        edit.commit();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_keyword_text = search_keyword.getText().toString();
                if (!search_keyword_text.isEmpty()) {
                    searchInitiate();
                    if (isNetworkAvailable()) {
                        loadFirstPage();
                    } else {
                        loadDataFromCache();
                    }

                    dismissKeyboard();
                } else {
                    search_keyword.requestFocus();
                    showKeyboard();
                }
            }
        });

        search_keyword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search_keyword_text = search_keyword.getText().toString();
                    if (!search_keyword_text.isEmpty()) {
                        searchInitiate();
                        if (isNetworkAvailable()) {
                            loadFirstPage();
                        } else {
                            loadDataFromCache();
                        }

                        dismissKeyboard();
                    } else {
                        search_keyword.requestFocus();
                        showKeyboard();
                    }
                    return true;
                }
                return false;
            }
        });

        // Set Layout Manager
        GridLayoutManager = new GridLayoutManager(this, 2);

        image_recycler.setLayoutManager(GridLayoutManager);

        setRecyclerView(list);

        image_recycler.addOnScrollListener(new PaginationScrollListener(GridLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 10;

                // mocking network delay for API call
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextPage();
                    }
                }, 100);
            }

            @Override
            public int getTotalPageCount() {
                return 10;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

    }

    public void getIds() {
        toolbar = findViewById(R.id.toolbar);
        search = findViewById(R.id.search);
        search_keyword = findViewById(R.id.search_keyword);
        image_recycler = findViewById(R.id.image_list);
        progressBar = findViewById(R.id.progressBar);
        info_text = findViewById(R.id.info_text);
        main_layout = findViewById(R.id.main_layout);
        info_layout = findViewById(R.id.info_layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_two:
                GridLayoutManager =
                        new GridLayoutManager(this, 2);
                image_recycler.setLayoutManager(GridLayoutManager);
                break;

            case R.id.action_three:
                GridLayoutManager =
                        new GridLayoutManager(this, 3);
                image_recycler.setLayoutManager(GridLayoutManager);
                break;

            case R.id.action_four:
                GridLayoutManager =
                        new GridLayoutManager(this, 4);
                image_recycler.setLayoutManager(GridLayoutManager);
                break;

            default:
                break;
        }

        return true;
    }

    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != getCurrentFocus()) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), 0);
        }
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(search_keyword, 0);
    }

    public void volleyGetImageData() {
        info_text.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);

                try {
                    JSONObject reader = new JSONObject(response);
                    parseData(reader);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int statusCode = error.networkResponse.statusCode;
                progressBar.setVisibility(View.GONE);
                CustomSnackbar customSnackbar;

                if (statusCode==400){
                    isLastPage = true;
//                    customSnackbar = new CustomSnackbar(MainActivity.this,main_layout,getString(R.string.search_limit),"");
                }else if (statusCode==403){
                    customSnackbar = new CustomSnackbar(MainActivity.this,main_layout,getString(R.string.search_limit),"");
                } else {
                    customSnackbar = new CustomSnackbar(MainActivity.this, main_layout, getString(R.string.something_went_wrong), "");
                }
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }

            @Override
            public Request.Priority getPriority() {
                return Request.Priority.HIGH;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, "stringRequest");
    }

    public void setRecyclerView(ArrayList<ImageObj> list) {
        adapter = new RecyclerAdapter(MainActivity.this, list);
        image_recycler.setAdapter(adapter);
    }

    private void loadFirstPage() {
        list.clear();
        generateURL();
        if (isNetworkAvailable()) {
            volleyGetImageData();
        } else {
            loadDataFromCache();
        }

    }

    private void loadNextPage() {
        generateURL();
        if (isNetworkAvailable()) {
            volleyGetImageData();
        } else {
            loadDataFromCache();
        }

        isLoading = false;
    }

    public boolean isNetworkAvailable() {
        boolean state;
        ConnectivityManager cmg = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cmg.getActiveNetworkInfo();
        state = activeNetworkInfo != null && activeNetworkInfo.isConnected();

        if (state) {
            return true;
        } else {
            //NO interntet
            return false;
        }
    }

    public JSONObject getVolleyCacheEntryByUrl() {
        // RequestQueue queue = Volley.newRequestQueue(c);
        String cachedResponse = new String(AppController
                .getInstance()
                .getRequestQueue()
                .getCache()
                .get(url).data);

        try {
            JSONObject cacheObj = new JSONObject(cachedResponse);
            Log.e("CacheResult", cacheObj.toString());
            return cacheObj;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    private void loadDataFromCache() {
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(url);
        if (entry != null) {
            try {
                //Your function to parses the data
                parseData(getVolleyCacheEntryByUrl());
            } catch (NullPointerException e) {
                //Handle exception
            }
        } else {
            info_text.setText(getResources().getString(R.string.no_internet_info_text));
//            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void generateURL() {
        String cx = "&cx=" + CONST.cx;
        String APIKey = "&key=" + CONST.API_KEY;
        String query = "&q=".concat(search_keyword_text);
        String start = "&start=".concat(String.valueOf(currentPage));
        url = "https://www.googleapis.com/customsearch/v1?&searchType=image&imgSize=xlarge&alt=json&num=10"
                .concat(APIKey)
                .concat(cx)
                .concat(query)
                .concat(start);
    }

    public void parseData(JSONObject reader) {
        String startindex;
        String imgLink;
        String thumbnailLink;

        try {
            JSONObject queries = reader.getJSONObject("queries");
            if (queries.has("nextPage")) {
                JSONArray nextPage = queries.getJSONArray("nextPage");
                JSONObject obj = nextPage.getJSONObject(0);
                startindex = obj.getString("startIndex");
            }

            JSONArray items = reader.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                ImageObj imageObj = new ImageObj();

                JSONObject obj = items.getJSONObject(i);
                imgLink = obj.getString("link");

                JSONObject image = obj.getJSONObject("image");
                thumbnailLink = image.getString("thumbnailLink");

                imageObj.setPreview_url(imgLink);
                imageObj.setThumb_url(thumbnailLink);

                list.add(imageObj);
            }
            adapter.add();
            image_recycler.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void searchInitiate(){
        list.clear();
        currentPage = 1;
        isLastPage = false;
        image_recycler.setItemAnimator(null);
        image_recycler.setVisibility(View.GONE);
        info_layout.setVisibility(View.GONE);
    }
}