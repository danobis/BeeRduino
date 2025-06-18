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

const mqttBrokerUrl = 'wss://test.mosquitto.org:8081'; // Public MQTT over WebSocket broker
const topic = '/beehive/data/hive01';

function App() {
  const [dataPoints, setDataPoints] = useState([]);
  const [temperature, setTemperature] = useState(null);
  const [humidity, setHumidity] = useState(null);
  const [weight, setWeight] = useState(null);

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
        const timeLabel = new Date().toLocaleTimeString();

        if (payload.temperature) setTemperature(payload.temperature);
        if (payload.humidity) setHumidity(payload.humidity);
        if (payload.weight) setWeight(payload.weight);

        setDataPoints((prev) => {
          const newPoints = [...prev, { time: timeLabel, temp: payload.temperature }];
          return newPoints.length > 20 ? newPoints.slice(newPoints.length - 20) : newPoints;
        });
      } catch (e) {
        console.error('Error parsing MQTT message', e);
      }
    });

    return () => client.end();
  }, []);

  const chartData = {
    labels: dataPoints.map((dp) => dp.time),
    datasets: [
      {
        label: 'Temperature (°C)',
        data: dataPoints.map((dp) => dp.temp),
        fill: false,
        borderColor: 'rgb(255, 206, 86)',
        tension: 0.3,
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: { position: 'top' },
      title: { display: true, text: 'Temperature Over Time' },
    },
  };

  return (
    <div style={{ maxWidth: '700px', margin: '2rem auto', fontFamily: 'Arial, sans-serif' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '1rem', marginBottom: '1rem' }}>
        <img src="/beeRduino.png" alt="BeeRduino Logo" style={{ height: '200px' }} />
      </div>
      <div style={{ display: 'flex', justifyContent: 'space-around', marginBottom: '2rem' }}>
        <div>
          <h3>Temperature</h3>
          <p style={{ fontSize: '2rem', margin: 0 }}>{temperature !== null ? temperature + ' °C' : '-'}</p>
        </div>
        <div>
          <h3>Humidity</h3>
          <p style={{ fontSize: '2rem', margin: 0 }}>{humidity !== null ? humidity + ' %' : '-'}</p>
        </div>
        <div>
          <h3>Weight</h3>
          <p style={{ fontSize: '2rem', margin: 0 }}>{weight !== null ? weight + ' kg' : '-'}</p>
        </div>
      </div>
      <Line options={chartOptions} data={chartData} />
      <p style={{ marginTop: '1rem', fontSize: '0.9rem', color: '#666' }}>
        Data updates live from MQTT topic: <code>{topic}</code>
      </p>
    </div>
  );
}

export default App;
