#!/usr/bin/env python3
"""
AKShare数据服务 - 提供HTTP API获取真实股票数据
"""
from flask import Flask, jsonify, request
import akshare as ak
import pandas as pd
from datetime import datetime, timedelta
import json
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

def stock_code_format(stock_code):
    """格式化股票代码"""
    stock_code = stock_code.strip()
    if stock_code.startswith('6'):
        return f"sh{stock_code}"
    elif stock_code.startswith(('0', '3')):
        return f"sz{stock_code}"
    else:
        return stock_code

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
        
        # AKShare的股票代码格式
        symbol = stock_code_format(stock_code)
        
        # 获取K线数据
        df = ak.stock_zh_a_hist(symbol=symbol, period=period, 
                               start_date=start_date, end_date=end_date)
        
        # 转换数据格式
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
            'stockCode': stock_code
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
        
        # 获取实时行情
        df = ak.stock_zh_a_spot_em()
        stock_data = df[df['代码'] == stock_code]
        
        if stock_data.empty:
            return jsonify({
                'success': False,
                'error': '股票代码不存在'
            }), 404
        
        row = stock_data.iloc[0]
        
        return jsonify({
            'success': True,
            'data': {
                'stockCode': stock_code,
                'stockName': row['名称'],
                'currentPrice': float(row['最新价']),
                'changeAmount': float(row['涨跌额']),
                'changePercent': float(row['涨跌幅']),
                'openPrice': float(row['今开']),
                'highPrice': float(row['最高']),
                'lowPrice': float(row['最低']),
                'volume': int(row['成交量']),
                'turnover': float(row['成交额']),
                'marketCap': float(row['总市值']) if '总市值' in row else 0,
                'pe': float(row['市盈率-动态']) if '市盈率-动态' in row else 0
            }
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/batch-quote', methods=['POST'])
def get_batch_quote():
    """批量获取行情"""
    try:
        codes = request.json.get('codes', [])
        
        df = ak.stock_zh_a_spot_em()
        results = []
        
        for code in codes:
            stock_data = df[df['代码'] == code]
            if not stock_data.empty:
                row = stock_data.iloc[0]
                results.append({
                    'stockCode': code,
                    'stockName': row['名称'],
                    'currentPrice': float(row['最新价']),
                    'changeAmount': float(row['涨跌额']),
                    'changePercent': float(row['涨跌幅']),
                    'volume': int(row['成交量'])
                })
        
        return jsonify({
            'success': True,
            'data': results
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/stock-info/<stock_code>', methods=['GET'])
def get_stock_info(stock_code):
    """获取股票基本信息"""
    try:
        symbol = stock_code_format(stock_code)
        
        # 获取股票基本信息
        df = ak.stock_individual_info_em(symbol=symbol)
        
        info = {}
        for _, row in df.iterrows():
            info[row['item']] = row['value']
        
        return jsonify({
            'success': True,
            'data': {
                'stockCode': stock_code,
                'stockName': info.get('股票简称', ''),
                'industry': info.get('行业', ''),
                'market': info.get('市场', ''),
                'totalShares': info.get('总股本', ''),
                'floatShares': info.get('流通股本', ''),
                'listingDate': info.get('上市时间', '')
            }
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/health', methods=['GET'])
def health():
    """健康检查"""
    return jsonify({'status': 'ok'})

if __name__ == '__main__':
    print("启动AKShare数据服务...")
    print("访问 http://localhost:5001/api/kline/600519 查看贵州茅台K线")
    print("访问 http://localhost:5001/api/quote/600519 查看贵州茅台实时行情")
    app.run(host='0.0.0.0', port=5001, debug=False)
