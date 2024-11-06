package shticell.client.component.sheet.center;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import exception.InvalidXMLFormatException;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sheet.coordinate.Coordinate;
import shticell.client.util.Constants;
import shticell.client.util.http.HttpClientUtil;
import shticell.client.util.http.HttpMethod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ServerEngineService {

    public CompletableFuture<SheetDTO> getSheet(String sheetName) {
        // Create a CompletableFuture that will hold the result in the future
        CompletableFuture<SheetDTO> future = new CompletableFuture<>();

        // Construct the URL
        String url = Constants.GET_SHEET_BY_NAME + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8);

        // Make an asynchronous call to fetch the sheet data
        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.GET, null, (responseBody) -> {
            if (responseBody == null) {
                future.completeExceptionally(new RuntimeException("Empty response from server"));
                return;
            }


            try {
                // Parse the response JSON into a Map to access "sheetDTO" and "permissionType"
                Map<String, Object> responseMap = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, Map.class);
                SheetDTO sheetDTO = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(Constants.GSON_INSTANCE_WITH_DESERIALIZERS.toJson(responseMap.get("sheetDTO")), SheetDTO.class);
                if (sheetDTO != null) {
                    future.complete(sheetDTO);
                } else {
                    future.completeExceptionally(new RuntimeException("Deserialization error"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });

        // Return the future immediately, even though it’s not completed yet
        return future;
    }


    public void setBackgroundColor(String sheetName, int row, int col, Color color) {
        // Endpoint URL for setting background color
        String url = Constants.SET_BACKGROUND_COLOR + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&row=" + row + "&col=" + col;

        // Convert the Color to a hex string (e.g., "#RRGGBB") or an appropriate format
        String colorHex = String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));

        // Create JSON payload
        String jsonPayload = "{\"color\": \"" + colorHex + "\"}";
        RequestBody requestBody = RequestBody.create(jsonPayload, MediaType.parse("application/json"));

        // Send an asynchronous request
        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.POST, requestBody, (responseBody) -> {
            if (responseBody == null) {
                System.out.println("Failed to set background color: Empty response from server.");
            } else {
                System.out.println("Background color updated successfully.");
            }
        });
    }

    public void setTextColor(String sheetName, int row, int col, Color color) {
        // Endpoint URL for setting text color
        String url = Constants.SET_TEXT_COLOR + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&row=" + row + "&col=" + col;

        // Convert the Color to a hex string (e.g., "#RRGGBB")
        String colorHex = String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));

        // Create JSON payload
        String jsonPayload = "{\"color\": \"" + colorHex + "\"}";
        RequestBody requestBody = RequestBody.create(jsonPayload, MediaType.parse("application/json"));

        // Send an asynchronous request
        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.POST, requestBody, (responseBody) -> {
            if (responseBody == null) {
                System.out.println("Failed to set text color: Empty response from server.");
            } else {
                System.out.println("Text color updated successfully.");
            }
        });
    }

    public void undoColor(String sheetName, int row, int col) {
        // Endpoint URL for setting text color
        String url = Constants.UNDO_COLOR + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&row=" + row + "&col=" + col;

        // Use GET method
        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.GET, null, (responseBody) -> {
            if (responseBody == null) {
                System.out.println("Failed to undo color: Empty response from server.");
            } else {
                System.out.println("Undo color updated successfully. Response: " + responseBody);
            }
        });
    }

    public CompletableFuture<SheetDTO> sortSheet(String sheetName, String fromCell, String toCell, List<Integer> columnsToSortBy) {
        CompletableFuture<SheetDTO> future = new CompletableFuture<>();

        // Construct the URL with parameters
        String url = Constants.SORT_SHEET_URL
                + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&fromCell=" + URLEncoder.encode(fromCell, StandardCharsets.UTF_8)
                + "&toCell=" + URLEncoder.encode(toCell, StandardCharsets.UTF_8);

        // Convert columnsToSortBy list to JSON format
        String columnsJson = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.toJson(columnsToSortBy);
        RequestBody requestBody = RequestBody.create(columnsJson, MediaType.parse("application/json"));

        // Asynchronous HTTP call
        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.POST, requestBody, (responseBody) -> {
            if (responseBody == null) {
                future.completeExceptionally(new RuntimeException("Failed to sort sheet: Empty response from server."));
            } else {
                SheetDTO sheetDTO = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, SheetDTO.class);
                future.complete(sheetDTO);
            }
        });

        return future;
    }

    public CompletableFuture<SheetDTO> filterSheet(String sheetName, String fromCell, String toCell, Set<String> selectedWordsSet) {
        CompletableFuture<SheetDTO> future = new CompletableFuture<>();

        // Construct the URL with parameters
        String url = Constants.FILTER_SHEET_URL
                + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&fromCell=" + URLEncoder.encode(fromCell, StandardCharsets.UTF_8)
                + "&toCell=" + URLEncoder.encode(toCell, StandardCharsets.UTF_8);

        // Convert selectedWordsSet to JSON format
        String wordsJson = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.toJson(selectedWordsSet);
        RequestBody requestBody = RequestBody.create(wordsJson, MediaType.parse("application/json"));

        // Asynchronous HTTP call
        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.POST, requestBody, (responseBody) -> {
            if (responseBody == null) {
                future.completeExceptionally(new RuntimeException("Failed to filter sheet: Empty response from server."));
            } else {
                SheetDTO sheetDTO = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, SheetDTO.class);
                future.complete(sheetDTO);
            }
        });

        return future;
    }

    public void setCell(String sheetName, int row, int column, String input) {
        String url = Constants.SET_CELL_URL + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&row=" + row + "&column=" + column;

        // Create JSON payload with the input value
        String jsonPayload = "{\"input\": \"" + input + "\"}";
        RequestBody requestBody = RequestBody.create(jsonPayload, MediaType.parse("application/json"));

        // Send asynchronous request to set the cell value
        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.POST, requestBody, (responseBody) -> {
            if (responseBody == null) {
                System.out.println("Failed to set cell value: Empty response from server.");
            } else {
                System.out.println("Cell updated successfully.");
            }
        });
    }

    public CompletableFuture<SheetDTO> applyDynamicAnalysis(String sheetName, Coordinate coordinate, Number newValue) {
        CompletableFuture<SheetDTO> result = new CompletableFuture<>();

        try {
            String url = Constants.APPLY_DYNAMIC_ANALYSIS_URL
                    + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                    + "&row=" + coordinate.getRow()
                    + "&col=" + coordinate.getColumn();

            // Create JSON payload for the new value
            String jsonPayload = "{\"newValue\": " + newValue + "}";
            RequestBody requestBody = RequestBody.create(jsonPayload, MediaType.parse("application/json"));

            // Send asynchronous request to apply dynamic analysis
            HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.POST, requestBody, (responseBody) -> {
                if (responseBody == null) {
                    System.out.println("Failed to apply dynamic analysis: Empty response from server.");
                    result.completeExceptionally(new RuntimeException("Server response was empty"));
                } else {
                    SheetDTO updatedSheet = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, SheetDTO.class);
                    result.complete(updatedSheet);
                }
            });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }

        return result;
    }

    public CompletableFuture<List<Coordinate>> createNewRange(String sheetName, String rangeName, String fromCell, String toCell) {
        String urlString = Constants.CREATE_NEW_RANGE_URL
                + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&rangeName=" + URLEncoder.encode(rangeName, StandardCharsets.UTF_8)
                + "&fromCell=" + URLEncoder.encode(fromCell, StandardCharsets.UTF_8)
                + "&toCell=" + URLEncoder.encode(toCell, StandardCharsets.UTF_8);

        CompletableFuture<List<Coordinate>> future = new CompletableFuture<>();
        RequestBody requestBody = RequestBody.create("{}", MediaType.parse("application/json")); // Non-null empty JSON body

        HttpClientUtil.runReqAsyncWithJson(urlString, HttpMethod.POST, requestBody, responseBody -> {
            if (responseBody == null) {
                future.completeExceptionally(new RuntimeException("Failed to create range: Empty response from server."));
                return;
            }

            try {
                // First, check if the response contains an error key
                if (responseBody.trim().startsWith("{")) {
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (jsonResponse.has("error")) {
                        String errorMessage = jsonResponse.get("error").getAsString();
                        future.completeExceptionally(new IllegalArgumentException(errorMessage));
                        return;
                    }
                }

                // If no "error" key exists, assume it's a valid list of coordinates
                Type coordinateListType = new TypeToken<List<Coordinate>>() {}.getType();
                List<Coordinate> coordinates = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, coordinateListType);
                future.complete(coordinates);
            } catch (Exception e) {
                future.completeExceptionally(new RuntimeException("Unexpected response format: " + e.getMessage()));
            }
        });

        return future;
    }



    public CompletableFuture<Void> removeRange(String sheetName, String selectedRange) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        String url = Constants.REMOVE_RANGE_URL
                + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&rangeName=" + URLEncoder.encode(selectedRange, StandardCharsets.UTF_8);

        RequestBody requestBody = RequestBody.create("", MediaType.parse("application/json"));

        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.POST, requestBody, (responseBody) -> {
            if (responseBody == null) {
                future.completeExceptionally(new RuntimeException("Server response was empty"));
            } else if (responseBody.contains("success")) {
                System.out.println("Range removed successfully.");
                future.complete(null); // Indicate success
            } else {
                future.completeExceptionally(new IllegalArgumentException(responseBody));
            }
        });

        return future;
    }


    public CompletableFuture<Optional<CellDTO>> getCell(String sheetName, int row, int column) {
        CompletableFuture<Optional<CellDTO>> future = new CompletableFuture<>();
        String url = Constants.GET_CELL_URL
                + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&row=" + row
                + "&column=" + column;

        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.GET, null, (responseBody) -> {
            if (responseBody == null || responseBody.trim().equals("{}")) {
                // No cell found, complete with an empty Optional
                future.complete(Optional.empty());
                return;
            }

            try {
                CellDTO cellDTO = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, CellDTO.class);
                future.complete(Optional.of(cellDTO));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }


    public CompletableFuture<List<Coordinate>> validateRange(String sheetName, String fromCell, String toCell) {
        CompletableFuture<List<Coordinate>> future = new CompletableFuture<>();

        // Check if the input cells are valid (both should have a column letter and a row number)
        if (!isValidCell(fromCell) || !isValidCell(toCell)) {
            // Ignore and complete the future with an empty list
            future.complete(Collections.emptyList());
            return future;
        }

        try {
            // Construct the URL with parameters
            String urlString = Constants.VALIDATE_RANGE_URL
                    + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                    + "&fromCell=" + URLEncoder.encode(fromCell, StandardCharsets.UTF_8)
                    + "&toCell=" + URLEncoder.encode(toCell, StandardCharsets.UTF_8);

            // Send the request asynchronously
            HttpClientUtil.runReqAsyncWithJson(urlString, HttpMethod.GET, null, responseBody -> {
                if (responseBody == null) {
                    future.completeExceptionally(new RuntimeException("Failed to validate range: Empty response from server."));
                } else {
                    // Deserialize the response to a list of Coordinates
                    List<Coordinate> rangeCoordinates = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, new TypeToken<List<Coordinate>>() {}.getType());
                    future.complete(rangeCoordinates);
                }
            });

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    // Helper method to check if a cell reference is valid, e.g., "C3" or "D10"
    private boolean isValidCell(String cell) {
        return cell != null && cell.matches("^[A-Za-z]+\\d+$");
    }


    public CompletableFuture<List<Double>> getRangeNumericValues(String sheetName, List<Coordinate> range) {
        CompletableFuture<List<Double>> future = new CompletableFuture<>();

        try {
            // Construct the URL with the sheet name as a parameter
            String urlString = Constants.GET_RANGE_NUMERIC_VALUES_URL
                    + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8);

            // Convert the range to JSON format
            String rangeJson = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.toJson(range);
            RequestBody requestBody = RequestBody.create(rangeJson, MediaType.parse("application/json"));

            // Send an asynchronous POST request
            HttpClientUtil.runReqAsyncWithJson(urlString, HttpMethod.POST, requestBody, responseBody -> {
                if (responseBody == null) {
                    future.completeExceptionally(new RuntimeException("Failed to retrieve range values: Empty response from server."));
                } else {
                    // Deserialize the response to a list of Double values
                    List<Double> numericValues = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, new TypeToken<List<Double>>() {}.getType());
                    future.complete(numericValues);
                }
            });

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    public CompletableFuture<Set<String>> getWordsFromColumnAndRange(String sheetName, String column, String fromCellFieldFilter, String toCellFieldFilter) {
        if (!isValidCellFormat(fromCellFieldFilter) || !isValidCellFormat(toCellFieldFilter)) {
            // Skip the request if the format is invalid
            CompletableFuture<Set<String>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Invalid cell range format."));
            return future;
        }

        // Proceed with the request if the format is valid
        String url = Constants.GET_WORDS_FROM_COLUMN_AND_RANGE_URL
                + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&column=" + URLEncoder.encode(column, StandardCharsets.UTF_8)
                + "&fromCell=" + URLEncoder.encode(fromCellFieldFilter, StandardCharsets.UTF_8)
                + "&toCell=" + URLEncoder.encode(toCellFieldFilter, StandardCharsets.UTF_8);

        CompletableFuture<Set<String>> future = new CompletableFuture<>();

        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.GET, null, (responseBody) -> {
            if (responseBody == null) {
                System.err.println("Failed to retrieve words: Empty response from server.");
                future.completeExceptionally(new RuntimeException("Empty response from server"));
            } else {
                Set<String> wordsSet = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, new TypeToken<Set<String>>() {}.getType());
                future.complete(wordsSet);
            }
        });

        return future;
    }

    private boolean isValidCellFormat(String cell) {
        return cell != null && cell.matches("^[A-Za-z]+\\d+$"); // e.g., "B2" or "C5"
    }

    public CompletableFuture<SheetDTO> peekVersion(String sheetName, int version) {
        String url = Constants.PEEK_VERSION_URL
                + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&version=" + version;

        CompletableFuture<SheetDTO> future = new CompletableFuture<>();

        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.GET, null, (responseBody) -> {
            if (responseBody == null) {
                System.err.println("Failed to retrieve sheet version: Empty response from server.");
                future.completeExceptionally(new RuntimeException("Empty response from server"));
            } else {
                SheetDTO sheetDTO = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, SheetDTO.class);
                future.complete(sheetDTO);
            }
        });

        return future;
    }

    public CompletableFuture<Map<Integer, SheetDTO>> getVersionHistory(String sheetName) {
        String url = Constants.GET_VERSION_HISTORY_URL + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8);

        CompletableFuture<Map<Integer, SheetDTO>> future = new CompletableFuture<>();

        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.GET, null, (responseBody) -> {
            if (responseBody == null) {
                System.err.println("Failed to retrieve version history: Empty response from server.");
                future.completeExceptionally(new RuntimeException("Empty response from server"));
            } else {
                Type versionHistoryType = new TypeToken<Map<Integer, SheetDTO>>(){}.getType();
                Map<Integer, SheetDTO> versionHistory = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, versionHistoryType);
                future.complete(versionHistory);
            }
        });

        return future;
    }
}

