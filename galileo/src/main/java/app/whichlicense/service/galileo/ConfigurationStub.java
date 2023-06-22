package app.whichlicense.service.galileo;

import com.whichlicense.configuration.KeyedConfiguration;

import java.util.function.Consumer;

public class ConfigurationStub implements KeyedConfiguration {
    @Override
    public boolean getBoolean(String s) {
        return false;
    }

    @Override
    public void hasBoolean(String s, Consumer<Boolean> consumer) {

    }

    @Override
    public int getInteger(String s) {
        return 0;
    }

    @Override
    public void hasInteger(String s, Consumer<Integer> consumer) {

    }

    @Override
    public long getLong(String s) {
        return 0;
    }

    @Override
    public void hasLong(String s, Consumer<Long> consumer) {

    }

    @Override
    public String getString(String s) {
        return null;
    }

    @Override
    public void hasString(String s, Consumer<String> consumer) {

    }

    @Override
    public void setBoolean(String s, boolean b) {

    }

    @Override
    public void setInteger(String s, int i) {

    }

    @Override
    public void setLong(String s, long l) {

    }

    @Override
    public void setString(String s, String s1) {

    }
}
