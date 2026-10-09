package com.nextrade.client.security; import java.util.*; public interface TokenStorage {void save(String access,String refresh);Optional<String> access();Optional<String> refresh();void clear();}
