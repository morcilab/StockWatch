package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.core.client.JavaScriptObject;

public class StockData extends JavaScriptObject {

    protected StockData() {}

    public final native String getSymbol() /*-{ return this.symbol; }-*/;
    public final native double getPrice() /*-{ return this.price; }-*/;
    public final native double getChange() /*-{ return this.change; }-*/;

    public final double getChangePercent() {
        return 100.0 * getChange() / getPrice();
    }
}
