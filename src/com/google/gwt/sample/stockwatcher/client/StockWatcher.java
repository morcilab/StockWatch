package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.Timer;

public class StockWatcher implements EntryPoint {

    private static final int REFRESH_INTERVAL = 5000; // ms
    private VerticalPanel mainPanel = new VerticalPanel();
    private FlexTable stocksFlexTable = new FlexTable();
    private HorizontalPanel addPanel = new HorizontalPanel();
	private HorizontalPanel persistPanel = new HorizontalPanel();
    private TextBox newSymbolTextBox = new TextBox();
    private Button addStockButton = new Button("Add");
	private Button loadSymbolsButton = new Button("Load");
	private Button saveSymbolsButton = new Button("Save");
    private Label lastUpdatedLabel = new Label();
    private List<String> stocks = new ArrayList<>();
    private StockPriceServiceAsync stockPriceSvc = GWT.create(StockPriceService.class);
    private Label errorMsgLabel = new Label();

    /**
     * Entry point method.
     */
    public void onModuleLoad() {
        // Create table for stock data.
        stocksFlexTable.setText(0, 0, "Symbol");
        stocksFlexTable.setText(0, 1, "Price");
        stocksFlexTable.setText(0, 2, "Change");
        stocksFlexTable.setText(0, 3, "Remove");
        stocksFlexTable.setCellPadding(6);

        stocksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
        stocksFlexTable.addStyleName("watchList");
        stocksFlexTable.getCellFormatter().addStyleName(0, 1, "watchListNumericColumn");
        stocksFlexTable.getCellFormatter().addStyleName(0, 2, "watchListNumericColumn");
        stocksFlexTable.getCellFormatter().addStyleName(0, 3, "watchListRemoveColumn");

        // Assemble Add Stock panel.
        addPanel.add(newSymbolTextBox);
        addPanel.add(addStockButton);
        addPanel.addStyleName("addPanel");

		// Assemble Persistence panel
		persistPanel.add(saveSymbolsButton);
		persistPanel.add(loadSymbolsButton);

		// Assemble Main panel.
        errorMsgLabel.setStyleName("errorMessage");
        errorMsgLabel.setVisible(false);

        mainPanel.add(errorMsgLabel);
        mainPanel.add(stocksFlexTable);
        mainPanel.add(addPanel);
		mainPanel.add(persistPanel);
        mainPanel.add(lastUpdatedLabel);

        // Associate the Main panel with the HTML host page.
        RootPanel.get("stockList").add(mainPanel);

        // Move cursor focus to the input box.
        newSymbolTextBox.setFocus(true);

        Timer refreshTimer = new Timer() {
            @Override
            public void run() {
                refreshWatchList();
            }
        };
        refreshTimer.scheduleRepeating(REFRESH_INTERVAL);

        addStockButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
            	String symbol = newSymbolTextBox.getText().toUpperCase().trim();
                addStock(symbol);
            }
        });

        newSymbolTextBox.addKeyDownHandler(new KeyDownHandler() {
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                	String symbol = newSymbolTextBox.getText().toUpperCase().trim();
                    addStock(symbol);
                }
            }
        });
        
		loadSymbolsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				stockPriceSvc.loadSymbols(new AsyncCallback<String[]>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Cannot load symbols: "
								+ caught.getMessage());
					}

					@Override
					public void onSuccess(String[] symbols) {
						for (String symbol : symbols) {
							addStock(symbol);
						}
					}
				});
			}
		});

		saveSymbolsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				stockPriceSvc.saveSymbols(stocks.toArray(new String[0]),
						new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Window.alert("Cannot load symbols: "
										+ caught.getMessage());
							}

							@Override
							public void onSuccess(Void result) {
								Window.alert("Symbols saved");
							}
						});
			}
		});

    }

    private void addStock(final String symbol) {
        newSymbolTextBox.setFocus(true);

        if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
            Window.alert("'" + symbol + "' is not a valid symbol.");
            return;
        }

        newSymbolTextBox.setText("");

        if (stocks.contains(symbol)) {
            return;
        }
        int row = stocksFlexTable.getRowCount();
        stocks.add(symbol);
        stocksFlexTable.setText(row, 0, symbol);
        stocksFlexTable.setWidget(row, 2, new Label());
        stocksFlexTable.getCellFormatter().addStyleName(row, 1, "watchListNumericColumn");
        stocksFlexTable.getCellFormatter().addStyleName(row, 2, "watchListNumericColumn");
        stocksFlexTable.getCellFormatter().addStyleName(row, 3, "watchListRemoveColumn");

        Button removeStockButton = new Button("x");
        removeStockButton.addStyleDependentName("remove");
        removeStockButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                int removedIndex = stocks.indexOf(symbol);
                stocks.remove(removedIndex);
                stocksFlexTable.removeRow(removedIndex + 1);
            }
        });
        stocksFlexTable.setWidget(row, 3, removeStockButton);

        refreshWatchList();
    }

    private void refreshWatchList() {
		AsyncCallback<StockPrice[]> callback = new AsyncCallback<StockPrice[]>() {
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onSuccess(StockPrice[] result) {
				updateTable(result);
			}
		};

		stockPriceSvc.getPrices(stocks.toArray(new String[0]), callback);
    }

    private void updateTable(StockPrice[] prices) {
		for (int i = 0; i < prices.length; i++) {
			updateTable(prices[i]);
		}
		// Display timestamp showing last refresh.
		lastUpdatedLabel.setText("Last update : "
				+ DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM)
						.format(new Date()));
    }

    private void updateTable(StockPrice price) {
        if (!stocks.contains(price.getSymbol())) {
            return;
        }

        int row = stocks.indexOf(price.getSymbol()) + 1;

        // Format the data in the Price and Change fields.
        String priceText = NumberFormat.getFormat("#,##0.00").format(
                price.getPrice());
        NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
        String changeText = changeFormat.format(price.getChange());
        String changePercentText = changeFormat.format(price.getChangePercent());

        // Populate the Price and Change fields with new data.
        stocksFlexTable.setText(row, 1, priceText);
        Label changeWidget = (Label) stocksFlexTable.getWidget(row, 2);
        changeWidget.setText(changeText + " (" + changePercentText + "%)");

        String changeStyleName = "noChange";
        if (price.getChangePercent() < -0.1f) {
            changeStyleName = "negativeChange";
        } else if (price.getChangePercent() > 0.1f) {
            changeStyleName = "positiveChange";
        }
        changeWidget.setStyleName(changeStyleName);
    }
}