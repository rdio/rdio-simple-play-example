package com.rdio.simple;

public abstract class ConsumerCredentials {
  // you can get these by signing up for a developer account at:
  // http://developer.rdio.com/
  public static String RDIO_CONSUMER_KEY = "";
  public static String RDIO_CONSUMER_SECRET = "";

  static {
    // read the consumer key and secret from environment variables
    ConsumerCredentials.RDIO_CONSUMER_KEY = System.getenv("RDIO_CONSUMER_KEY");
    ConsumerCredentials.RDIO_CONSUMER_SECRET = System.getenv("RDIO_CONSUMER_SECRET");
  }
}
