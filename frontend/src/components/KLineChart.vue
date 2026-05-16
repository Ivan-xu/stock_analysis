<template>
  <div ref="chartContainer" style="width: 100%; height: 400px;"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  kLines: {
    type: Array,
    required: true
  }
})

const chartContainer = ref(null)
let chartInstance = null

const initChart = () => {
  if (!chartContainer.value) {
    console.error('Chart container not found')
    return
  }
  
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  chartInstance = echarts.init(chartContainer.value)
  console.log('ECharts initialized, data count:', props.kLines?.length || 0)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance) {
    console.warn('Chart not initialized')
    return
  }
  
  if (!props.kLines || props.kLines.length === 0) {
    console.warn('No K-line data')
    return
  }
  
  console.log('Updating chart with', props.kLines.length, 'data points')
  
  const dates = props.kLines.map(k => k.date)
  const data = props.kLines.map(k => [k.open, k.close, k.low, k.high])
  
  const option = {
    title: {
      text: 'K线走势'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    grid: {
      left: '10%',
      right: '10%',
      top: '15%',
      bottom: '15%'
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: true
    },
    yAxis: {
      type: 'value',
      scale: true
    },
    series: [
      {
        name: 'K线',
        type: 'candlestick',
        data: data,
        itemStyle: {
          color: '#ef5350',
          color0: '#26a69a',
          borderColor: '#ef5350',
          borderColor0: '#26a69a'
        }
      }
    ]
  }
  
  chartInstance.setOption(option)
  console.log('Chart updated successfully')
}

onMounted(() => {
  setTimeout(() => {
    initChart()
  }, 100)
})

watch(() => props.kLines, (newData) => {
  console.log('K-lines data changed:', newData?.length || 0)
  if (chartInstance) {
    updateChart()
  } else {
    initChart()
  }
}, { immediate: false, deep: true })

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

window.addEventListener('resize', () => {
  if (chartInstance) {
    chartInstance.resize()
  }
})
</script>
