package shticell.client.util.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

public class HttpClientUtil {

    private final static SimpleCookieManager simpleCookieManager = new SimpleCookieManager();
    private final static OkHttpClient HTTP_CLIENT =
            new OkHttpClient.Builder()
                    .cookieJar(simpleCookieManager)
                    .followRedirects(false)
                    .build();

    public static void setCookieManagerLoggingFacility(Consumer<String> logConsumer) {
        simpleCookieManager.setLogData(logConsumer);
    }

    public static void removeCookiesOf(String domain) {
        simpleCookieManager.removeCookiesOf(domain);
    }

    public static void runAsync(String finalUrl, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static void runAsyncRequest(Request request, Callback callback) {
        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);
        call.enqueue(callback);  // Asynchronously send the request
    }

    public static void runReqAsyncWithJson(
            String finalUrl,
            HttpMethod methodType,
            RequestBody requestBody,
            Consumer<String> responseConsumer) {

        Request.Builder requestBuilder = new Request.Builder().url(finalUrl);

        // Set the request method type (GET, POST, etc.)
        getRequestType(methodType, requestBody, requestBuilder);

        Request request = requestBuilder.build();

        // Process the request asynchronously
        processTheRequest(responseConsumer, request);
    }

    private static void getRequestType(HttpMethod methodType, RequestBody requestBody, Request.Builder requestBuilder) {
        switch (methodType) {
            case POST:
                requestBuilder.post(requestBody);
                break;
            case PUT:
                requestBuilder.put(requestBody);
                break;
            case DELETE:
                requestBuilder.delete(requestBody);
                break;
            case PATCH:
                requestBuilder.patch(requestBody);
                break;
            case GET:
                requestBuilder.get();
                break;
            default:
                throw new IllegalArgumentException("Invalid HTTP method: " + methodType);
        }
    }

    private static void processTheRequest(Consumer<String> responseConsumer, Request request) {
        Callback callback = new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.code() == 204) {  // No Content
                        responseConsumer.accept(null);
                    } else {
                        String json = response.body().string();
                        if (response.isSuccessful() && json != null) {
                            responseConsumer.accept(json);
                        } else {
                            JsonObject errorObject = JsonParser.parseString(json).getAsJsonObject();
                            String errorMessage = errorObject.get("error").getAsString();
                            handleFailure(new IOException(errorMessage));
                        }
                    }
                } finally {
                    response.close();
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleFailure(e);
            }

            private void handleFailure(IOException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("HTTP Request Error");
                    alert.setHeaderText("An error occurred during the HTTP request.");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                });
            }
        };

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);
        call.enqueue(callback);  // Send the request asynchronously
    }


    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }
}
