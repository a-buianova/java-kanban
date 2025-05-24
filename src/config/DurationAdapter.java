package config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

/**
 * Адаптер для сериализации и десериализации java.time.Duration в Gson.
 * Преобразует Duration в строку формата ISO: "PT30M" и обратно.
 */
public class DurationAdapter extends TypeAdapter<Duration> {

    @Override
    public void write(JsonWriter out, Duration value) throws IOException {
        // Serialize as ISO-8601 string, e.g. "PT15M"
        out.value(value.toString());
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        // Parse back from ISO-8601
        return Duration.parse(in.nextString());
    }
}