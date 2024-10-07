package com.example.stocklookupapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Define views used in the layout
    private EditText searchStock;           // Input field for stock symbol
    private Button btnSearch;                // Button to trigger search
    private TextView stockName;              // TextView to display stock name
    private TextView stockPrice;             // TextView to display stock price
    private TextView percentageChange;       // TextView to display percentage change
    private ProgressBar progressBar;         // ProgressBar to indicate loading status

    // OkHttpClient instance for making network requests
    private OkHttpClient client;

    // Replace with your actual EODHD API key
    private static final String API_KEY = "6703ada7c6b595.86628314";
    private static final String BASE_URL = "https://eodhistoricaldata.com/api/real-time/"; // Base URL for the API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout for this activity

        // Initialize the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set the toolbar as the app bar

        // Set the title for the app bar
        getSupportActionBar().setTitle("Stock Lookup");

        // Initialize views
        searchStock = findViewById(R.id.searchStock); // EditText for entering stock symbol
        btnSearch = findViewById(R.id.btnSearch);     // Button to search for stock data
        stockName = findViewById(R.id.stockName);     // TextView to display the stock name
        stockPrice = findViewById(R.id.stockPrice);   // TextView to display the stock price
        percentageChange = findViewById(R.id.percentageChange); // TextView to display percentage change
        progressBar = findViewById(R.id.progressBar); // Initialize ProgressBar for loading status

        client = new OkHttpClient(); // Create a new instance of OkHttpClient

        // Set an OnClickListener for the search button
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the stock symbol from the EditText
                String stockSymbol = searchStock.getText().toString().trim();
                // Fetch stock data using the entered symbol
                fetchStockData(stockSymbol);
            }
        });
    }

    // Method to fetch stock data from the API
    private void fetchStockData(String stockSymbol) {
        // Show the ProgressBar while loading data
        progressBar.setVisibility(View.VISIBLE);

        // Construct the API URL with the stock symbol
        String url = BASE_URL + stockSymbol + "?api_token=" + API_KEY + "&fmt=csv"; // Ensure it's CSV format

        // Create a new HTTP request
        Request request = new Request.Builder()
                .url(url) // Set the request URL
                .get()    // Use GET method
                .build(); // Build the request

        // Execute the network request in a new thread
        new Thread(() -> {
            try {
                // Perform the network call
                Response response = client.newCall(request).execute();
                // Check if the response is successful
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Read the response body as a string
                String csvResponse = response.body().string();
                Log.d("StockData", csvResponse);  // Log the CSV response for debugging

                // Parse the CSV response on the main thread
                runOnUiThread(() -> {
                    try {
                        // Split the CSV data by line break and then by commas
                        String[] lines = csvResponse.split("\n");
                        String[] stockData = lines[1].split(",");  // Second line holds the actual data

                        // Extract stock data from the parsed CSV
                        String name = stockData[0];           // Stock ticker
                        String price = stockData[6];          // Closing price
                        String change = stockData[10];        // Percentage change

                        // Update the TextViews with stock data
                        stockName.setText("Stock Name: " + name);
                        stockPrice.setText("Price: $" + price);
                        percentageChange.setText("Change: " + change + "%");

                    } catch (Exception e) {
                        Log.e("CSVParsingError", "Error parsing CSV", e);
                        stockName.setText("Error parsing data");
                        stockPrice.setText("");
                        percentageChange.setText("");
                    } finally {
                        // Hide the ProgressBar after processing the data
                        progressBar.setVisibility(View.GONE);
                    }
                });
            } catch (IOException e) {
                Log.e("NetworkError", "Failed to fetch data", e);
                runOnUiThread(() -> {
                    stockName.setText("Error fetching data");
                    stockPrice.setText("");
                    percentageChange.setText("");

                    // Hide the ProgressBar in case of error
                    progressBar.setVisibility(View.GONE);
                });
            }
        }).start(); // Start the new thread
    }
}
