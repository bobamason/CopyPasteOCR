package net.masonapps.shippingdataocr;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Bob Mason on 7/25/2018.
 */
public class CommClient implements Closeable {

    private static final String TAG = CommClient.class.getSimpleName();
    private BufferedReader reader = null;
    private BufferedWriter writer = null;
    private Socket socket = null;

    public CompletableFuture<Boolean> init() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Log.d(TAG, "socket created");
                socket = new Socket("192.168.1.201", Constants.PORT);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                Log.d(TAG, "reader and writer created");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "init error occurred " + e.getLocalizedMessage());
                e.printStackTrace();
            }
            return false;
        });
    }

    public CompletableFuture<String> send(final String string) {
        return CompletableFuture.supplyAsync(() -> {
            Log.d(TAG, "sending string " + string);
            String response = "";
            try {
                writer.write(string);
                writer.newLine();
                writer.flush();
                Log.d(TAG, "string sent");

                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
            } catch (Exception e) {
                Log.e(TAG, "send error occurred " + e.getLocalizedMessage());
                e.printStackTrace();
            }
            return response;
        });
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }
        if (reader != null) {
            reader.close();
            reader = null;
        }
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }
}
