import React, { useEffect, useState } from 'react';
import mqtt from 'mqtt';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Line } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const mqttBrokerUrl = 'wss://test.mosquitto.org:8081'; // Public MQTT broker
const topic = '/beehive/data/hive01';

// BeeRduino brand colors
const colors = {
  yellow: '#FFCC00',
  darkGreen: '#003329',
  alertRed: '#D32F2F',
  normalText: '#111',
};

function isOutOfRange(value, min, max) {
  return value !== null && (value < min || value > max);
}

function formatTimestamp(date) {
  return date ? date.toLocaleTimeString() : '-';
}

function App() {
  const [dataPoints, setDataPoints] = useState({
    temperature: [],
    humidity: [],
    weight: [],
  });
  const [latestData, setLatestData] = useState({
    temperature: null,
    humidity: null,
    weight: null,
    lastUpdate: null,
  });

  useEffect(() => {
    const client = mqtt.connect(mqttBrokerUrl);

    client.on('connect', () => {
      console.log('Connected to MQTT broker');
      client.subscribe(topic, (err) => {
        if (err) {
          console.error('Subscribe error:', err);
        }
      });
    });

    client.on('message', (topic, message) => {
      try {
        const payload = JSON.parse(message.toString());
        const timeLabel = new Date();

        setLatestData((prev) => ({
          temperature: payload.temperature ?? prev.temperature,
          humidity: payload.humidity ?? prev.humidity,
          weight: payload.weight ?? prev.weight,
          lastUpdate: timeLabel,
        }));

        setDataPoints((prev) => ({
          temperature: [...prev.temperature, { time: timeLabel, value: payload.temperature }].slice(-20),
          humidity: [...prev.humidity, { time: timeLabel, value: payload.humidity }].slice(-20),
          weight: [...prev.weight, { time: timeLabel, value: payload.weight }].slice(-20),
        }));
      } catch (e) {
        console.error('Error parsing MQTT message', e);
      }
    });

    return () => client.end();
  }, []);

  const makeChartData = (label, data, color) => ({
    labels: data.map((dp) => formatTimestamp(dp.time)),
    datasets: [
      {
        label,
        data: data.map((dp) => dp.value),
        fill: false,
        borderColor: color,
        backgroundColor: color,
        tension: 0.3,
      },
    ],
  });

  const chartOptions = (title) => ({
    responsive: true,
    plugins: {
      legend: { position: 'top', labels: { color: colors.darkGreen } },
      title: { display: true, text: title, color: colors.darkGreen, font: { size: 16 } },
    },
    scales: {
      x: { ticks: { color: colors.darkGreen } },
      y: { ticks: { color: colors.darkGreen }, beginAtZero: true },
    },
  });

  return (
    <div style={{ maxWidth: 900, margin: '2rem auto', fontFamily: 'Arial, sans-serif', padding: '0 1rem' }}>
      <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1rem' }}>
        <img src="/beeRduino.png" alt="BeeRduino Logo" style={{ height: 250 }} />
      </div>

      <div
        style={{
          display: 'flex',
          justifyContent: 'space-around',
          marginBottom: '2rem',
          flexWrap: 'wrap',
          gap: '2rem',
        }}
      >
        {['temperature', 'humidity', 'weight'].map((metric) => {
          const value = latestData[metric];
          let unit = '';
          let min, max;
          if (metric === 'temperature') {
            unit = '°C';
            min = 0;
            max = 50;
          } else if (metric === 'humidity') {
            unit = '%';
            min = 20;
            max = 90;
          } else if (metric === 'weight') {
            unit = 'kg';
            min = 0;
            max = 100;
          }

          const outOfRange = isOutOfRange(value, min, max);
          return (
            <div
              key={metric}
              style={{
                flex: '1 1 150px',
                textAlign: 'center',
                padding: '1rem',
                border: `2px solid ${outOfRange ? colors.alertRed : colors.darkGreen}`,
                borderRadius: 8,
                boxShadow: outOfRange ? `0 0 10px ${colors.alertRed}` : `0 0 10px ${colors.darkGreen}`,
              }}
            >
              <h3 style={{ margin: 0, color: colors.darkGreen, fontWeight: 'bold' }}>
                {metric.charAt(0).toUpperCase() + metric.slice(1)}
              </h3>
              <p
                style={{
                  fontSize: '2rem',
                  margin: '0.5rem 0',
                  color: outOfRange ? colors.alertRed : colors.darkGreen,
                  fontWeight: 'bold',
                }}
              >
                {value !== null ? value.toFixed(1) + ' ' + unit : '-'}
              </p>
              <p style={{ fontSize: '0.8rem', color: '#555' }}>
                Last update: {formatTimestamp(latestData.lastUpdate)}
              </p>
              {outOfRange && <p style={{ color: colors.alertRed }}>⚠️ Value out of range!</p>}
            </div>
          );
        })}
      </div>

      <div style={{ marginBottom: '3rem' }}>
        <Line data={makeChartData('Temperature (°C)', dataPoints.temperature, colors.yellow)} options={chartOptions('Temperature Over Time')} />
      </div>
      <div style={{ marginBottom: '3rem' }}>
        <Line data={makeChartData('Humidity (%)', dataPoints.humidity, colors.darkGreen)} options={chartOptions('Humidity Over Time')} />
      </div>
      <div style={{ marginBottom: '3rem' }}>
        <Line data={makeChartData('Weight (kg)', dataPoints.weight, colors.darkGreen)} options={chartOptions('Weight Over Time')} />
      </div>

      <p style={{ marginTop: '1rem', fontSize: '0.9rem', color: '#666', textAlign: 'center' }}>
        Data updates live from MQTT topic: <code>{topic}</code>
      </p>
    </div>
  );
}

export default App;
