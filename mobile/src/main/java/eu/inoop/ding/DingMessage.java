package eu.inoop.ding;


import android.support.annotation.NonNull;

public final class DingMessage {

    @NonNull
    private final String mKey;
    private final float mPrice;
    @NonNull
    private final String mDesc;

    public DingMessage(@NonNull String key, float price, @NonNull String desc) {
        mKey = key;
        mPrice = price;
        mDesc = desc;
    }

    @NonNull
    public String getKey() {
        return mKey;
    }

    public float getPrice() {
        return mPrice;
    }

    @NonNull
    public String getDesc() {
        return mDesc;
    }

    @Override
    public String toString() {
        return "DingMessage{" +
                "mKey='" + mKey + '\'' +
                ", mPrice=" + mPrice +
                ", mDesc='" + mDesc + '\'' +
                '}';
    }
}
