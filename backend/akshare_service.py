#!/usr/bin/env python3
"""
AKShare数据服务 - 提供HTTP API获取真实股票数据
包含Mock数据降级方案
"""
from flask import Flask, jsonify, request
import akshare as ak
import pandas as pd
from datetime import datetime, timedelta
import random
from flask_cors import CORS
import warnings
warnings.filterwarnings('ignore')

app = Flask(__name__)
CORS(app)

# Mock数据基础价格（模拟真实市场）
MOCK_BASE_PRICES = {
    '600519': 1700.0,  # 贵州茅台
    '000858': 150.0,   # 五粮液
    '601318': 45.0,    # 中国平安
    '000001': 12.0,    # 平安银行
    '600036': 35.0,    # 招商银行
    '000333': 60.0,    # 美的集团
    '002594': 250.0,   # 比亚迪
}

STOCK_NAMES = {
    '600519': '贵州茅台',
    '000858': '五粮液',
    '601318': '中国平安',
    '000001': '平安银行',
    '600036': '招商银行',
    '000333': '美的集团',
    '002594': '比亚迪',
    '600276': '恒瑞医药',
    '300750': '宁德时代',
    '688981': '中芯国际',
}

def stock_code_format(stock_code):
    """格式化股票代码"""
    stock_code = stock_code.strip()
    if stock_code.startswith('6'):
        return f"sh{stock_code}"
    elif stock_code.startswith(('0', '3')):
        return f"sz{stock_code}"
    else:
        return stock_code

def generate_mock_klines(stock_code, days=365):
    """生成Mock K线数据"""
    base_price = MOCK_BASE_PRICES.get(stock_code, 100.0)
    stock_name = STOCK_NAMES.get(stock_code, '未知股票')

    klines = []
    current_price = base_price

    for i in range(days, 0, -1):
        date = datetime.now() - timedelta(days=i)

        # 模拟价格波动
        change_percent = random.uniform(-0.03, 0.03)
        open_price = current_price * (1 + random.uniform(-0.02, 0.02))
        close_price = open_price * (1 + change_percent)
        high_price = max(open_price, close_price) * (1 + random.uniform(0, 0.02))
        low_price = min(open_price, close_price) * (1 - random.uniform(0, 0.02))

        # 模拟成交量
        volume = random.randint(1000000, 50000000)

        klines.append({
            'date': date.strftime('%Y-%m-%d'),
            'open': round(open_price, 2),
            'high': round(high_price, 2),
            'low': round(low_price, 2),
            'close': round(close_price, 2),
            'volume': volume,
            'turnover': round(volume * (open_price + close_price) / 2, 2)
        })

        current_price = close_price

    return klines

def generate_mock_quote(stock_code):
    """生成Mock实时行情"""
    base_price = MOCK_BASE_PRICES.get(stock_code, 100.0)
    stock_name = STOCK_NAMES.get(stock_code, '未知股票')

    # 模拟价格波动
    change_amount = base_price * random.uniform(-0.05, 0.05)
    current_price = base_price + change_amount
    change_percent = (change_amount / base_price) * 100

    return {
        'stockCode': stock_code,
        'stockName': stock_name,
        'currentPrice': round(current_price, 2),
        'changeAmount': round(change_amount, 2),
        'changePercent': round(change_percent, 2),
        'openPrice': round(base_price * (1 + random.uniform(-0.01, 0.01)), 2),
        'highPrice': round(current_price * (1 + random.uniform(0, 0.03)), 2),
        'lowPrice': round(current_price * (1 - random.uniform(0, 0.03)), 2),
        'volume': random.randint(1000000, 50000000),
        'turnover': round(random.uniform(1e9, 5e10), 2),
        'marketCap': round(current_price * random.randint(10, 100) * 1e8, 2),
        'pe': round(random.uniform(10, 50), 2)
    }

@app.route('/api/kline/<stock_code>', methods=['GET'])
def get_kline(stock_code):
    """获取K线数据"""
    try:
        period = request.args.get('period', 'daily')
        start_date = request.args.get('start', '')
        end_date = request.args.get('end', '')

        # 默认获取近一年的数据
        if not end_date:
            end_date = datetime.now().strftime('%Y%m%d')
        if not start_date:
            start_date = (datetime.now() - timedelta(days=365)).strftime('%Y%m%d')

        # 尝试从AKShare获取真实数据
        symbol = stock_code_format(stock_code)

        try:
            df = ak.stock_zh_a_hist(symbol=symbol, period=period,
                                   start_date=start_date, end_date=end_date, adjust="qfq")

            klines = []
            for _, row in df.iterrows():
                klines.append({
                    'date': row['日期'],
                    'open': float(row['开盘']),
                    'high': float(row['最高']),
                    'low': float(row['最低']),
                    'close': float(row['收盘']),
                    'volume': int(row['成交量']),
                    'turnover': float(row['成交额']) if '成交额' in row else 0
                })

            return jsonify({
                'success': True,
                'data': klines,
                'stockCode': stock_code,
                'source': 'akshare'
            })

        except Exception as akshare_error:
            # AKShare失败时使用Mock数据
            print(f"AKShare failed: {akshare_error}, using mock data")
            mock_klines = generate_mock_klines(stock_code, 365)

            return jsonify({
                'success': True,
                'data': mock_klines,
                'stockCode': stock_code,
                'source': 'mock'
            })

    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/quote/<stock_code>', methods=['GET'])
def get_realtime_quote(stock_code):
    """获取实时行情"""
    try:
        symbol = stock_code_format(stock_code)

        # 尝试从AKShare获取真实数据
        try:
            df = ak.stock_zh_a_spot_em()
            stock_data = df[df['代码'].astype(str) == str(stock_code)]

            if not stock_data.empty:
                row = stock_data.iloc[0]

                return jsonify({
                    'success': True,
                    'data': {
                        'stockCode': str(stock_code),
                        'stockName': str(row['名称']),
                        'currentPrice': float(row['最新价']) if pd.notna(row['最新价']) else 0.0,
                        'changeAmount': float(row['涨跌额']) if pd.notna(row['涨跌额']) else 0.0,
                        'changePercent': float(row['涨跌幅']) if pd.notna(row['涨跌幅']) else 0.0,
                        'openPrice': float(row['今开']) if pd.notna(row['今开']) else 0.0,
                        'highPrice': float(row['最高']) if pd.notna(row['最高']) else 0.0,
                        'lowPrice': float(row['最低']) if pd.notna(row['最低']) else 0.0,
                        'volume': int(row['成交量']) if pd.notna(row['成交量']) else 0,
                        'turnover': float(row['成交额']) if pd.notna(row['成交额']) else 0.0,
                        'marketCap': float(row['总市值']) if '总市值' in row and pd.notna(row['总市值']) else 0.0,
                        'pe': float(row['市盈率-动态']) if '市盈率-动态' in row and pd.notna(row['市盈率-动态']) else 0.0
                    },
                    'source': 'akshare'
                })

        except Exception as akshare_error:
            print(f"AKShare failed: {akshare_error}, using mock data")

        # 使用Mock数据
        mock_quote = generate_mock_quote(stock_code)

        return jsonify({
            'success': True,
            'data': mock_quote,
            'source': 'mock'
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'error': f'获取行情失败: {str(e)}'
        }), 500

@app.route('/api/batch-quote', methods=['POST'])
def get_batch_quote():
    """批量获取行情"""
    try:
        codes = request.json.get('codes', [])

        results = []

        for code in codes:
            mock_quote = generate_mock_quote(code)
            results.append(mock_quote)

        return jsonify({
            'success': True,
            'data': results,
            'source': 'mock'
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/stock-info/<stock_code>', methods=['GET'])
def get_stock_info(stock_code):
    """获取股票基本信息"""
    stock_name = STOCK_NAMES.get(stock_code, '未知股票')

    return jsonify({
        'success': True,
        'data': {
            'stockCode': stock_code,
            'stockName': stock_name,
            'industry': '未知行业',
            'market': 'A股',
            'totalShares': '未知',
            'floatShares': '未知',
            'listingDate': '未知'
        }
    })

@app.route('/health', methods=['GET'])
def health():
    """健康检查"""
    return jsonify({'status': 'ok'})

if __name__ == '__main__':
    print("启动AKShare数据服务（带Mock数据降级）...")
    print("访问 http://localhost:5001/api/kline/600519 查看贵州茅台K线")
    print("访问 http://localhost:5001/api/quote/600519 查看贵州茅台实时行情")
    print("注意：当真实数据获取失败时，系统会自动降级到Mock数据")
    app.run(host='0.0.0.0', port=5001, debug=False, threaded=True)
