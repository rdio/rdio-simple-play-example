package com.rdio.simple;

public abstract class ConsumerCredentials {
  public final static Rdio.Consumer consumer;
  static {
    // read the consumer key and secret from environment variables
    String key = System.getenv("RDIO_CONSUMER_KEY");
    String secret = System.getenv("RDIO_CONSUMER_SECRET");
    if (key == null || secret == null) {
      System.err.println("Missing RDIO_CONSUMER_KEY or RDIO_CONSUMER_SECRET");
      consumer = null;
    } else {
      consumer = new Rdio.Consumer(key, secret);
    }
  }
}
