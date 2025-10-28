package com.example.ai_diary.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paging")
public class PagingProperties {
	  private int defaultSize = 20;
	  private int maxSize = 50;
	  public int getDefaultSize() { return defaultSize; }
	  public void setDefaultSize(int defaultSize) { this.defaultSize = defaultSize; }
	  public int getMaxSize() { return maxSize; }
	  public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
	}
