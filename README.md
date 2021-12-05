A StockWatch app using GWT based on [this tutorial](http://www.gwtproject.org/doc/latest/tutorial/gettingstarted.html).
It comes packaged with a JAR from GWT 2.9.0. You can probably compile it with a previous version of GWT but you will need to replace gwt-servlet.jar in war/WEB-INF/lib/.

If you check this out stratight from Eclipse your project will have hardcoded paths for the GTW JAR files. To fix this, simply go to Project/Properties/Java Build Path and remove all the broken JARs, then click on the "Add library..." button, select GTW and add GWT 2.9.0.
