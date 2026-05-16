-- 股票分析系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS stock_analysis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE stock_analysis;

-- 股票基础信息表
CREATE TABLE IF NOT EXISTS stock_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stock_code VARCHAR(10) NOT NULL UNIQUE,
    stock_name VARCHAR(100) NOT NULL,
    industry VARCHAR(50),
    market VARCHAR(20),
    total_shares DECIMAL(20, 2),
    float_shares DECIMAL(20, 2),
    listing_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_stock_code (stock_code),
    INDEX idx_industry (industry)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- K线数据表
CREATE TABLE IF NOT EXISTS kline_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stock_code VARCHAR(10) NOT NULL,
    trade_date DATE NOT NULL,
    open_price DECIMAL(10, 2),
    high_price DECIMAL(10, 2),
    low_price DECIMAL(10, 2),
    close_price DECIMAL(10, 2),
    volume BIGINT,
    turnover DECIMAL(20, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stock_date (stock_code, trade_date),
    INDEX idx_trade_date (trade_date),
    INDEX idx_stock_date (stock_code, trade_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 实时行情表
CREATE TABLE IF NOT EXISTS realtime_quote (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stock_code VARCHAR(10) NOT NULL UNIQUE,
    stock_name VARCHAR(100),
    current_price DECIMAL(10, 2),
    change_amount DECIMAL(10, 2),
    change_percent DECIMAL(10, 2),
    open_price DECIMAL(10, 2),
    high_price DECIMAL(10, 2),
    low_price DECIMAL(10, 2),
    volume BIGINT,
    turnover DECIMAL(20, 2),
    market_cap DECIMAL(20, 2),
    pe DECIMAL(10, 2),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 分析历史表
CREATE TABLE IF NOT EXISTS analysis_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(50) NOT NULL UNIQUE,
    stock_code VARCHAR(10) NOT NULL,
    stock_name VARCHAR(100),
    analysis_date DATE NOT NULL,
    recommendation VARCHAR(20),
    risk_level VARCHAR(20),
    target_price DECIMAL(10, 2),
    stop_loss DECIMAL(10, 2),
    confidence DECIMAL(5, 2),
    rsi DECIMAL(5, 2),
    macd_signal VARCHAR(50),
    sentiment VARCHAR(20),
    sentiment_intensity DECIMAL(5, 2),
    news_count INT,
    summary TEXT,
    key_factors TEXT,
    risk_factors TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_stock_code (stock_code),
    INDEX idx_analysis_date (analysis_date),
    INDEX idx_recommendation (recommendation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 自选股表（从原H2迁移）
CREATE TABLE IF NOT EXISTS watchlist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stock_code VARCHAR(10) NOT NULL,
    stock_name VARCHAR(100),
    add_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stock_code (stock_code),
    INDEX idx_add_time (add_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
