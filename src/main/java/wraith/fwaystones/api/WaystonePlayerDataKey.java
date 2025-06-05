package wraith.fwaystones.api;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructField;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class WaystonePlayerDataKey<T> {
    private static final Map<String, WaystonePlayerDataKey<?>> NAME_TO_KEY = new HashMap<>();

    public static Endec<WaystonePlayerDataKey<?>> ENDEC = Endec.STRING.xmap(NAME_TO_KEY::get, WaystonePlayerDataKey::name);

    private final String name;
    private final Endec<T> endec;
    private final Function<WaystonePlayerData, T> getter;

    WaystonePlayerDataKey(String name, Endec<T> endec, Function<WaystonePlayerData, T> getter) {
        if (NAME_TO_KEY.containsKey(name)) {
            throw new IllegalArgumentException("Unable to add DataKey for the given name as it already exists: " + name);
        }

        NAME_TO_KEY.put(name, this);

        this.name = name;
        this.endec = endec;
        this.getter = getter;
    }

    public T get(WaystonePlayerData data) {
        return getter.apply(data);
    }

    public StructField<WaystonePlayerData, T> optionalFieldOf(@Nullable T defaultValue) {
        return optionalFieldOf(() -> defaultValue);
    }

    public StructField<WaystonePlayerData, T> optionalFieldOf(Supplier<@Nullable T> defaultValue) {
        return new StructField<>(name, endec.optionalOf().xmap(optional -> optional.orElseGet(defaultValue), Optional::ofNullable), getter, defaultValue);
    }

    public String name() {
        return name;
    }

    public Endec<T> endec() {
        return endec;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WaystonePlayerDataKey) obj;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "DataKey[" +
                "name=" + name + ", " +
                "endec=" + endec + ']';
    }

}
