<template>
  <el-container style="height: 100vh;">
    <el-header style="background: #409EFF; color: white; display: flex; align-items: center;">
      <h1>📈 股票智能分析系统 - MVP</h1>
    </el-header>
    
    <el-container>
      <el-aside width="300px" style="padding: 20px; border-right: 1px solid #e6e6e6;">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>自选股</span>
              <el-button type="primary" size="small" @click="showAddDialog">+ 添加</el-button>
            </div>
          </template>
          
          <el-list :data="watchlist">
            <el-list-item 
              v-for="item in watchlist" 
              :key="item.id"
              style="cursor: pointer; padding: 10px 0;"
              @click="selectStock(item.stockCode)"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                  <div style="font-weight: bold;">{{ item.stockCode }}</div>
                  <div style="font-size: 12px; color: #666;">{{ item.stockName }}</div>
                </div>
                <el-button type="danger" size="small" @click.stop="removeFromWatchlist(item.id)">
                  删除
                </el-button>
              </div>
            </el-list-item>
          </el-list>
          
          <div v-if="watchlist.length === 0" style="text-align: center; color: #999; padding: 20px;">
            暂无自选股
          </div>
        </el-card>
      </el-aside>
      
      <el-main style="padding: 20px;">
        <el-card>
          <template #header>
            <div class="card-header" style="display: flex; gap: 10px; align-items: center;">
              <el-input 
                v-model="stockCode" 
                placeholder="输入股票代码 (如: 600519)" 
                style="width: 200px;"
              />
              <el-button type="primary" @click="analyzeStock" :loading="analyzing">
                开始分析
              </el-button>
              <el-button 
                type="success" 
                @click="downloadReport" 
                :disabled="!result"
              >
                📄 下载PDF报告
              </el-button>
            </div>
          </template>
          
          <div v-if="analyzing" style="margin-bottom: 20px;">
            <el-progress :percentage="progress" :status="status === 'complete' ? 'success' : ''" />
            <div style="margin-top: 10px; color: #666;">{{ progressMessage }}</div>
          </div>
          
          <div v-if="result">
            <el-alert 
              :title="`${result.stockName} (${result.stockCode}) - 分析结果`" 
              :type="getAlertType(result.investment.recommendation)"
              style="margin-bottom: 20px;"
            />
            
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card>
                  <div style="text-align: center;">
                    <div style="font-size: 24px; font-weight: bold; margin-bottom: 10px;">
                      {{ result.investment.recommendation }}
                    </div>
                    <div style="color: #666; margin-bottom: 10px;">
                      风险等级：{{ result.investment.riskLevel }}
                    </div>
                    <div style="color: #409EFF; font-size: 18px;">
                      目标价：¥{{ result.investment.targetPrice }}
                    </div>
                  </div>
                </el-card>
              </el-col>
              
              <el-col :span="8">
                <el-card>
                  <h4>技术分析</h4>
                  <div style="margin-top: 10px;">
                    <div>建议：{{ result.techAnalyst.recommendation }}</div>
                    <div style="margin-top: 5px;">
                      RSI：<span :style="{ color: getRSIColor(result.techAnalyst.rsi.rsi) }">{{ result.techAnalyst.rsi.rsi?.toFixed(2) }}</span>
                    </div>
                    <div style="margin-top: 5px; font-size: 12px; color: #666;">
                      {{ result.techAnalyst.rsi.suggestion }}
                    </div>
                  </div>
                </el-card>
              </el-col>
              
              <el-col :span="8">
                <el-card>
                  <h4>舆情分析</h4>
                  <div style="margin-top: 10px;">
                    <div>整体：<span style="color: #67C23A; font-weight: bold;">{{ result.sentiment.overallSentiment }}</span></div>
                    <div style="margin-top: 10px;">
                      <div style="font-weight: bold; margin-bottom: 5px;">相关新闻：</div>
                      <ul style="font-size: 12px; padding-left: 15px;">
                        <li v-for="(news, idx) in result.sentiment.news" :key="idx">{{ news }}</li>
                      </ul>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            
            <div style="margin-top: 20px;">
              <el-card>
                <template #header>
                  <span>K线走势图</span>
                </template>
                <KLineChart :kLines="result.researcher.klines" />
              </el-card>
            </div>
          </div>
          
          <div v-else-if="!analyzing" style="text-align: center; color: #999; padding: 50px;">
            输入股票代码，点击"开始分析"
          </div>
        </el-card>
      </el-main>
    </el-container>
    
    <el-dialog v-model="addDialogVisible" title="添加自选股" width="400px">
      <el-form>
        <el-form-item label="股票代码">
          <el-input v-model="newStock.stockCode" placeholder="如: 600519" />
        </el-form-item>
        <el-form-item label="股票名称">
          <el-input v-model="newStock.stockName" placeholder="如: 贵州茅台" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addToWatchlist">确认添加</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import KLineChart from './components/KLineChart.vue'
import axios from 'axios'

const stockCode = ref('')
const analyzing = ref(false)
const progress = ref(0)
const progressMessage = ref('')
const result = ref(null)
const status = ref('')

const watchlist = ref([])
const addDialogVisible = ref(false)
const newStock = ref({
  stockCode: '',
  stockName: ''
})

const stockNames = {
  '600519': '贵州茅台',
  '000858': '五粮液',
  '601318': '中国平安',
  '000001': '平安银行',
  '600036': '招商银行'
}

const loadWatchlist = async () => {
  try {
    const res = await axios.get('/api/watchlist')
    watchlist.value = res.data
  } catch (e) {
    console.error('加载自选股失败', e)
  }
}

const addToWatchlist = async () => {
  try {
    await axios.post('/api/watchlist', newStock.value)
    await loadWatchlist()
    addDialogVisible.value = false
    newStock.value = { stockCode: '', stockName: '' }
    ElMessage.success('添加成功')
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

const removeFromWatchlist = async (id) => {
  try {
    await axios.delete(`/api/watchlist/${id}`)
    await loadWatchlist()
    ElMessage.success('删除成功')
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const selectStock = (code) => {
  stockCode.value = code
}

const showAddDialog = () => {
  addDialogVisible.value = true
}

const analyzeStock = () => {
  if (!stockCode.value) {
    ElMessage.warning('请输入股票代码')
    return
  }
  
  analyzing.value = true
  progress.value = 0
  result.value = null
  status.value = ''
  
  const eventSource = new EventSource(`/api/analysis/stream/${stockCode.value}`)
  
  eventSource.addEventListener('progress', (event) => {
    const data = JSON.parse(event.data)
    progress.value = data.progress
    progressMessage.value = data.message
  })
  
  eventSource.addEventListener('complete', (event) => {
    const data = JSON.parse(event.data)
    result.value = data
    analyzing.value = false
    status.value = 'complete'
    eventSource.close()
  })
  
  eventSource.addEventListener('error', (event) => {
    console.error('SSE错误', event)
    analyzing.value = false
    ElMessage.error('分析失败')
    eventSource.close()
  })
}

const downloadReport = () => {
  window.location.href = `/api/analysis/pdf/${stockCode.value}`
}

const getAlertType = (recommendation) => {
  if (recommendation === '买入') return 'success'
  if (recommendation === '观望') return 'warning'
  return 'info'
}

const getRSIColor = (rsi) => {
  if (!rsi) return '#333'
  if (rsi > 70) return '#F56C6C'
  if (rsi < 30) return '#67C23A'
  return '#E6A23C'
}

onMounted(() => {
  loadWatchlist()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body {
  height: 100%;
}
</style>
