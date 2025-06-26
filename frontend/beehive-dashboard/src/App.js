// src/App.js
import React, { useEffect, useState } from 'react';
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
import { useQuery, gql, useSubscription } from '@apollo/client';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

// ---------- Konfiguration ----------

// Frontend-Keys (lower-case mit Präfix) für Anzeige: Labels, Farben, Units
const FRONT_KEYS = [
  'sensor_temperature_inside',
  'sensor_temperature_outside',
  'sensor_humidity_inside',
  'sensor_humidity_outside',
  'sensor_weight',
];

const SENSOR_COLORS = {
  sensor_temperature_inside: '#FF8C00',
  sensor_temperature_outside: '#1E90FF',
  sensor_humidity_inside: '#32CD32',
  sensor_humidity_outside: '#20B2AA',
  sensor_weight: '#8B4513',
};

const SENSOR_LABELS = {
  sensor_temperature_inside: 'Temperature Inside',
  sensor_temperature_outside: 'Temperature Outside',
  sensor_humidity_inside: 'Humidity Inside',
  sensor_humidity_outside: 'Humidity Outside',
  sensor_weight: 'Weight',
};

const SENSOR_CONFIG = {
  sensor_temperature_inside: { unit: '°C' },
  sensor_temperature_outside: { unit: '°C' },
  sensor_humidity_inside: { unit: '%' },
  sensor_humidity_outside: { unit: '%' },
  sensor_weight: { unit: 'kg' },
};

// Backend-Keys (uppercase) für State/Gruppierung und als Variable in Subscription
const BACKEND_KEYS = [
  'TEMPERATURE_INSIDE',
  'TEMPERATURE_OUTSIDE',
  'HUMIDITY_INSIDE',
  'HUMIDITY_OUTSIDE',
  'WEIGHT',
];

// mapSensorType: Wandelt Eingabe (lower-case oder uppercase) in uppercase-Enum um.
// Rückgabe: uppercase (z.B. "TEMPERATURE_INSIDE") oder null, wenn unbekannt.
function mapSensorType(sensorTypeBackend) {
  if (!sensorTypeBackend) {
    console.warn('mapSensorType: leere Eingabe');
    return null;
  }
  // Bereits uppercase?
  if (BACKEND_KEYS.includes(sensorTypeBackend)) {
    return sensorTypeBackend;
  }
  // lower-case-Fälle
  switch (sensorTypeBackend) {
    case 'sensor_temperature_inside':
      return 'TEMPERATURE_INSIDE';
    case 'sensor_temperature_outside':
      return 'TEMPERATURE_OUTSIDE';
    case 'sensor_humidity_inside':
      return 'HUMIDITY_INSIDE';
    case 'sensor_humidity_outside':
      return 'HUMIDITY_OUTSIDE';
    case 'sensor_weight':
      return 'WEIGHT';
    default:
      console.warn('mapSensorType: kein Mapping für', sensorTypeBackend);
      return null;
  }
}

// Reverse-Mapping: uppercase → lower-case, um Label/Farbe/Unit zu holen
const BACKEND_TO_FRONT = {};
FRONT_KEYS.forEach((frontKey) => {
  const be = mapSensorType(frontKey);
  if (be) {
    BACKEND_TO_FRONT[be] = frontKey;
  }
});
// Beispiel: BACKEND_TO_FRONT["TEMPERATURE_INSIDE"] === "sensor_temperature_inside"

// GraphQL-Query: historische Daten
const GET_MEASUREMENTS = gql`
  query GetMeasurements($beehive_uuid: String!, $history_span: HistorySpan) {
    get_measurements(beehive_uuid: $beehive_uuid, history_span: $history_span) {
      uuid
      value
      type       # angenommen liefert Backend "sensor_temperature_inside" oder "TEMPERATURE_INSIDE"
      unit
      timestamp
    }
  }
`;

// GraphQL-Subscription: eine Subscription pro Sensortyp, liefert timestamp, value, unit
const NEW_MEASUREMENT_SUBSCRIPTION = gql`
  subscription OnNewMeasurement($beehive_uuid: String!, $sensor_type: SensorType!) {
    on_measurement(beehive_uuid: $beehive_uuid, sensor_type: $sensor_type) {
      timestamp
      value
      unit
    }
  }
`;

// Hilfsfunktion: formatiert Date zu Zeitstring
function formatTimestamp(date) {
  if (!date) return '-';
  return date.toLocaleTimeString();
}

// ---------- Hauptkomponente ----------

function App() {
  // Beispiel UUID; passe ggf. dynamisch an oder aus Props/Context holen
  const beehiveUUID = "6c5a5066-07a8-4a21-8dc2-766cbc63f0eb";

  // State: dataPoints gruppiert nach uppercase-Keys, initial leere Arrays
  const [dataPoints, setDataPoints] = useState(() => {
    const init = {};
    BACKEND_KEYS.forEach((bk) => {
      init[bk] = [];
    });
    return init;
  });
  // State: latestData[bk] und latestData[bk + "_time"]
  const [latestData, setLatestData] = useState(() => {
    const init = {};
    BACKEND_KEYS.forEach((bk) => {
      init[bk] = undefined;
      init[bk + '_time'] = undefined;
    });
    return init;
  });

  // 1. Historische Daten per Query laden
  const { data, loading, error } = useQuery(GET_MEASUREMENTS, {
    variables: { beehive_uuid: beehiveUUID, history_span: "LAST_5DAYS" },
  });

  // 2. Live-Subscriptions: eine Hook pro Sensortyp (uppercase)
  const subTemperatureInside = useSubscription(NEW_MEASUREMENT_SUBSCRIPTION, {
    variables: { beehive_uuid: beehiveUUID, sensor_type: 'TEMPERATURE_INSIDE' },
  });
  const subTemperatureOutside = useSubscription(NEW_MEASUREMENT_SUBSCRIPTION, {
    variables: { beehive_uuid: beehiveUUID, sensor_type: 'TEMPERATURE_OUTSIDE' },
  });
  const subHumidityInside = useSubscription(NEW_MEASUREMENT_SUBSCRIPTION, {
    variables: { beehive_uuid: beehiveUUID, sensor_type: 'HUMIDITY_INSIDE' },
  });
  const subHumidityOutside = useSubscription(NEW_MEASUREMENT_SUBSCRIPTION, {
    variables: { beehive_uuid: beehiveUUID, sensor_type: 'HUMIDITY_OUTSIDE' },
  });
  const subWeight = useSubscription(NEW_MEASUREMENT_SUBSCRIPTION, {
    variables: { beehive_uuid: beehiveUUID, sensor_type: 'WEIGHT' },
  });

  // 3. Effekt: Verarbeitung historischer Daten, wenn Query-Daten ankommen
  useEffect(() => {
    if (!data?.get_measurements) return;
    const measures = data.get_measurements;
    if (!measures.length) return;

    console.log("Historische Daten empfangen:", measures.length);

    // Neu initialisieren
    const grouped = {};
    BACKEND_KEYS.forEach((bk) => {
      grouped[bk] = [];
    });
    const latest = {};

    measures.forEach((m) => {
      const time = new Date(m.timestamp);
      const mapped = mapSensorType(m.type);
      if (!mapped) {
        console.warn("Unbekannter Sensortyp in historischer Query:", m.type);
        return;
      }
      let value = m.value;
      if (mapped === "WEIGHT") {
        // Annahme: Backend liefert Gramm → in kg umrechnen
        value = value / 1000;
      }
      grouped[mapped].push({ time, value });
      if (!latest[mapped] || latest[mapped].time < time) {
        latest[mapped] = { time, value };
      }
    });

    setDataPoints(grouped);

    // latestData aktualisieren
    const latestDisplay = {};
    BACKEND_KEYS.forEach((bk) => {
      if (latest[bk]) {
        latestDisplay[bk] = latest[bk].value;
        latestDisplay[bk + '_time'] = latest[bk].time;
      } else {
        latestDisplay[bk] = undefined;
        latestDisplay[bk + '_time'] = undefined;
      }
    });
    setLatestData(latestDisplay);
  }, [data]);

  // 4. Effekt: Verarbeitung live Subscription-Daten
  useEffect(() => {
    // Hilfsfunktion, um ein Subscription-Result zu prüfen und in State zu integrieren.
    // Da die Subscription-Payload kein sensor_type-Feld mehr enthält, 
    // übergeben wir hier explizit den Sensortyp, den wir abonniert haben.
    function handleSubscriptionResult(subRes, mappedKey) {
      if (subRes.data?.on_measurement) {
        const m = subRes.data.on_measurement;
        console.log(`Live-Update für ${mappedKey} erhalten:`, m);

        // Da payload kein sensor_type enthält, nutzen wir mappedKey direkt.
        let value = m.value;
        if (mappedKey === "WEIGHT") {
          value = value / 1000;
        }
        const time = new Date(m.timestamp);

        // dataPoints updaten
        setDataPoints((prev) => {
          const updated = { ...prev };
          if (!Array.isArray(updated[mappedKey])) {
            updated[mappedKey] = [];
          }
          const newArr = [...updated[mappedKey], { time, value }];
          const MAX_POINTS = 100;
          if (newArr.length > MAX_POINTS) {
            newArr.shift();
          }
          updated[mappedKey] = newArr;
          return updated;
        });

        // latestData updaten
        setLatestData((prev) => ({
          ...prev,
          [mappedKey]: value,
          [mappedKey + '_time']: time,
        }));
      }
    }

    // Prüfe jede Subscription einzeln und übergebe den entsprechenden uppercase-Key
    handleSubscriptionResult(subTemperatureInside, 'TEMPERATURE_INSIDE');
    handleSubscriptionResult(subTemperatureOutside, 'TEMPERATURE_OUTSIDE');
    handleSubscriptionResult(subHumidityInside, 'HUMIDITY_INSIDE');
    handleSubscriptionResult(subHumidityOutside, 'HUMIDITY_OUTSIDE');
    handleSubscriptionResult(subWeight, 'WEIGHT');

    // Dependencies: Subscription-Result-Objekte
  }, [
    subTemperatureInside,
    subTemperatureOutside,
    subHumidityInside,
    subHumidityOutside,
    subWeight,
  ]);

  // 5. Chart-Daten-Erzeugung
  const makeChartData = (label, dataArray, color) => ({
    labels: dataArray.map((dp) => formatTimestamp(dp.time)),
    datasets: [
      {
        label,
        data: dataArray.map((dp) => dp.value),
        fill: false,
        borderColor: color,
        backgroundColor: color,
        tension: 0.3,
      },
    ],
  });

  const chartOptions = (title) => ({
    responsive: true,
    animation: {
      duration: 500,
      easing: 'easeOutQuart',
    },
    plugins: {
      legend: { position: 'top' },
      title: { display: true, text: title },
    },
    scales: {
      x: { ticks: { color: '#333' } },
      y: { ticks: { color: '#333' }, beginAtZero: true },
    },
  });

  // 6. Laden- und Fehlerzustand anzeigen
  if (loading) return <p>Loading historical data...</p>;
  if (error) return <p>Error loading data: {error.message}</p>;

  // 7. Render
  return (
    <div style={{ maxWidth: 900, margin: '2rem auto', fontFamily: 'Arial, sans-serif', padding: '0 1rem' }}>
      {/* Logo oder Header */}
      <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1rem' }}>
        <img src="/beeRduino.png" alt="BeeRduino Logo" style={{ height: 250 }} />
      </div>

      {/* Aktuelle Werte-Kacheln */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-around',
          marginBottom: '2rem',
          flexWrap: 'wrap',
          gap: '2rem',
        }}
      >
        {BACKEND_KEYS.map((bk) => {
          const frontKey = BACKEND_TO_FRONT[bk];
          const label = frontKey ? SENSOR_LABELS[frontKey] : bk;
          const config = frontKey ? SENSOR_CONFIG[frontKey] : {};
          const value = latestData[bk];
          const time = latestData[bk + '_time'];

          return (
            <div
              key={bk}
              style={{
                flex: '1 1 150px',
                textAlign: 'center',
                padding: '1rem',
                border: `2px solid #ccc`,
                borderRadius: 8,
              }}
            >
              <h3 style={{ margin: 0 }}>{label}</h3>
              <p style={{ fontSize: '2rem', margin: '0.5rem 0' }}>
                {value !== undefined && value !== null ? value.toFixed(1) + ' ' + (config.unit || '') : '-'}
              </p>
              <p style={{ fontSize: '0.8rem', color: '#555' }}>
                Last update: {formatTimestamp(time)}
              </p>
            </div>
          );
        })}
      </div>

      {/* Charts für jeden Sensor */}
      {BACKEND_KEYS.map((bk) => {
  const frontKey = BACKEND_TO_FRONT[bk];
  const label = frontKey ? SENSOR_LABELS[frontKey] : bk;
  const color = frontKey ? SENSOR_COLORS[frontKey] : '#333';
  const dataArr = dataPoints[bk] || [];

  return (
    <div style={{ marginBottom: '3rem' }} key={bk}>
      {/* Changed here: removed timestamp-based key */}
      <Line
        key={bk}
        data={makeChartData(`${label} Over Time`, dataArr, color)}
        options={chartOptions(`${label} Over Time`)}
      />
    </div>
      );
    })}
    </div>
  );
}



export default App;
