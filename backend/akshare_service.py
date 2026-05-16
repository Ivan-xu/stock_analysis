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
import requests
warnings.filterwarnings('ignore')

app = Flask(__name__)
CORS(app)

MOCK_BASE_PRICES = {
    '600519': 1700.0,
    '000858': 150.0,
    '601318': 45.0,
    '000001': 12.0,
    '600036': 35.0,
    '000333': 60.0,
    '002594': 250.0,
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

POSITIVE_KEYWORDS = ['涨', '利好', '增长', '盈利', '突破', '创新', '扩张', '合作', '买入', '推荐', '上调', '超预期', '业绩', '订单', '中标']
NEGATIVE_KEYWORDS = ['跌', '利空', '亏损', '风险', '下调', '预警', '违约', '处罚', '调查', '诉讼', '减持', '预警', '业绩下滑', '风险']
NEUTRAL_KEYWORDS = ['公告', '会议', '报告', '说明', '进展', '披露', '更正', '问询']

def stock_code_format(stock_code):
    stock_code = stock_code.strip()
    if stock_code.startswith('6'):
        return f"sh{stock_code}"
    elif stock_code.startswith(('0', '3')):
        return f"sz{stock_code}"
    else:
        return stock_code

def analyze_sentiment(text):
    if not text:
        return 'neutral'
    
    text_lower = text.lower()
    pos_count = sum(1 for kw in POSITIVE_KEYWORDS if kw in text)
    neg_count = sum(1 for kw in NEGATIVE_KEYWORDS if kw in text)
    neu_count = sum(1 for kw in NEUTRAL_KEYWORDS if kw in text)
    
    if pos_count > neg_count and pos_count > neu_count:
        return 'positive'
    elif neg_count > pos_count:
        return 'negative'
    else:
        return 'neutral'

def generate_mock_news(stock_code):
    """生成Mock新闻数据"""
    stock_name = STOCK_NAMES.get(stock_code, '未知股票')
    
    news_templates = [
        {'title': f'{stock_name}发布2024年业绩预告', 'content': f'{stock_name}预计2024年净利润同比增长30%', 'source': '财经网'},
        {'title': '行业分析师上调评级', 'content': f'多家券商上调{stock_name}目标价至新高', 'source': '证券时报'},
        {'title': '新产品上市获得市场好评', 'content': f'{stock_name}新产品在市场上反响热烈', 'source': '上海证券报'},
        {'title': f'{stock_name}发布重要公告', 'content': '公司将于近期召开股东大会', 'source': '公司公告'},
        {'title': '机构投资者调研', 'content': f'多家机构投资者对{stock_name}进行调研', 'source': '机构调研'},
    ]
    
    news_list = []
    base_date = datetime.now()
    
    for i, template in enumerate(news_templates):
        news_date = (base_date - timedelta(days=i)).strftime('%Y-%m-%d')
        sentiment = analyze_sentiment(template['title'] + template['content'])
        
        news_list.append({
            'date': news_date,
            'title': template['title'],
            'content': template['content'],
            'source': template['source'],
            'sentiment': sentiment
        })
    
    return news_list

def fetch_eastmoney_news(stock_code):
    """从东方财富获取新闻"""
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
        
        url = f'https://np-anotice-stock.eastmoney.com/api/security/ann?sr=-1&page_size=20&page_index=1&ann_type=SHA,CYB,SZA&code={stock_code}'
        
        response = requests.get(url, headers=headers, timeout=10)
        
        if response.status_code == 200:
            data = response.json()
            
            if data.get('data') and data['data'].get('list'):
                news_list = []
                for item in data['data']['list'][:10]:
                    title = item.get('title', '')
                    content = item.get('notice_content', title)
                    sentiment = analyze_sentiment(title + content)
                    
                    news_list.append({
                        'date': item.get('publish_time', '')[:10] if item.get('publish_time') else '',
                        'title': title,
                        'content': content[:200] if content else '',
                        'source': '东方财富',
                        'sentiment': sentiment
                    })
                
                return news_list
        
        return None
        
    except Exception as e:
        print(f"东方财富新闻获取失败: {e}")
        return None

def fetch_netease_news(stock_code):
    """从网易财经获取新闻"""
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
        
        url = f'https://money.163.com/special/00251U62/news_push_callback.py?callback=ntes_search_result_callback&scode={stock_code}&page=1&sortType=1&pageSize=10'
        
        response = requests.get(url, headers=headers, timeout=10)
        
        if response.status_code == 200:
            return None
        
        return None
        
    except Exception as e:
        print(f"网易财经新闻获取失败: {e}")
        return None

def fetch_sina_news(stock_code):
    """从新浪财经获取新闻"""
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
        
        url = f'https://vip.stock.finance.sina.com.cn/corp/go.php/vCB_AllBulletin/stockid/{stock_code}.phtml'
        
        response = requests.get(url, headers=headers, timeout=10)
        
        if response.status_code == 200:
            return None
        
        return None
        
    except Exception as e:
        print(f"新浪财经新闻获取失败: {e}")
        return None

def generate_mock_klines(stock_code, days=365):
    base_price = MOCK_BASE_PRICES.get(stock_code, 100.0)
    stock_name = STOCK_NAMES.get(stock_code, '未知股票')

    klines = []
    current_price = base_price

    for i in range(days, 0, -1):
        date = datetime.now() - timedelta(days=i)

        change_percent = random.uniform(-0.03, 0.03)
        open_price = current_price * (1 + random.uniform(-0.02, 0.02))
        close_price = open_price * (1 + change_percent)
        high_price = max(open_price, close_price) * (1 + random.uniform(0, 0.02))
        low_price = min(open_price, close_price) * (1 - random.uniform(0, 0.02))

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
    base_price = MOCK_BASE_PRICES.get(stock_code, 100.0)
    stock_name = STOCK_NAMES.get(stock_code, '未知股票')

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
    try:
        period = request.args.get('period', 'daily')
        start_date = request.args.get('start', '')
        end_date = request.args.get('end', '')

        if not end_date:
            end_date = datetime.now().strftime('%Y%m%d')
        if not start_date:
            start_date = (datetime.now() - timedelta(days=365)).strftime('%Y%m%d')

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
    try:
        symbol = stock_code_format(stock_code)

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

@app.route('/api/news/<stock_code>', methods=['GET'])
def get_news(stock_code):
    """获取财经新闻"""
    try:
        news_count = int(request.args.get('count', 10))
        
        news = fetch_eastmoney_news(stock_code)
        
        if not news:
            print(f"真实新闻获取失败，使用Mock数据")
            news = generate_mock_news(stock_code)
            source = 'mock'
        else:
            source = 'eastmoney'
        
        if len(news) > news_count:
            news = news[:news_count]
        
        return jsonify({
            'success': True,
            'data': news,
            'stockCode': stock_code,
            'source': source,
            'count': len(news)
        })

    except Exception as e:
        print(f"新闻获取失败: {e}")
        mock_news = generate_mock_news(stock_code)
        return jsonify({
            'success': True,
            'data': mock_news,
            'stockCode': stock_code,
            'source': 'mock',
            'count': len(mock_news),
            'error': str(e)
        })

@app.route('/api/sentiment/<stock_code>', methods=['GET'])
def get_sentiment(stock_code):
    """获取情感分析结果"""
    try:
        news = fetch_eastmoney_news(stock_code)
        
        if not news:
            print(f"真实新闻获取失败，使用Mock数据")
            news = generate_mock_news(stock_code)
            source = 'mock'
        else:
            source = 'eastmoney'
        
        positive_count = sum(1 for n in news if n['sentiment'] == 'positive')
        negative_count = sum(1 for n in news if n['sentiment'] == 'negative')
        neutral_count = sum(1 for n in news if n['sentiment'] == 'neutral')
        
        total = len(news)
        
        if positive_count > negative_count * 1.5:
            overall_sentiment = '积极'
            sentiment_score = 0.6 + (positive_count / total) * 0.4
        elif negative_count > positive_count * 1.5:
            overall_sentiment = '消极'
            sentiment_score = 0.2 + (negative_count / total) * 0.3
        else:
            overall_sentiment = '中性'
            sentiment_score = 0.4 + (positive_count - negative_count) / (total * 2)
        
        return jsonify({
            'success': True,
            'data': {
                'stockCode': stock_code,
                'overallSentiment': overall_sentiment,
                'sentimentScore': round(sentiment_score, 2),
                'positiveCount': positive_count,
                'negativeCount': negative_count,
                'neutralCount': neutral_count,
                'totalNews': total,
                'news': news[:10]
            },
            'source': source
        })

    except Exception as e:
        print(f"情感分析失败: {e}")
        mock_news = generate_mock_news(stock_code)
        return jsonify({
            'success': True,
            'data': {
                'stockCode': stock_code,
                'overallSentiment': '中性',
                'sentimentScore': 0.5,
                'positiveCount': 2,
                'negativeCount': 1,
                'neutralCount': 2,
                'totalNews': 5,
                'news': mock_news
            },
            'source': 'mock',
            'error': str(e)
        })

@app.route('/api/batch-quote', methods=['POST'])
def get_batch_quote():
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
    return jsonify({'status': 'ok'})

if __name__ == '__main__':
    print("启动AKShare数据服务（带Mock数据降级）...")
    print("访问 http://localhost:5001/api/kline/600519 查看贵州茅台K线")
    print("访问 http://localhost:5001/api/quote/600519 查看贵州茅台实时行情")
    print("访问 http://localhost:5001/api/news/600519 查看贵州茅台新闻")
    print("访问 http://localhost:5001/api/sentiment/600519 查看情感分析")
    print("注意：当真实数据获取失败时，系统会自动降级到Mock数据")
    app.run(host='0.0.0.0', port=5001, debug=False, threaded=True)
