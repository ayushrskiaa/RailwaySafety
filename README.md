# Railway Crossing Simulation Dashboard

A real-time dashboard for monitoring train movements and railway gate status, using Firebase Realtime Database for live data updates.

> **Note:**  
> This project was developed as a college semester project and was presented to professors, receiving appreciation from all. It demonstrates a significant impact on railway safety and accident prevention through real-time monitoring and automation.

## Features

- Live train status: approaching, moving, or crossed
- Gate status: open, closed, opening, or closing
- Current speed and ETA display
- Chronological event log
- Responsive web UI

## Installation

```bash
git clone https://github.com/yourusername/railway-crossing-simulation.git
cd railway-crossing-simulation
```

## Usage

1. Update the Firebase configuration in `index.html` with your project credentials.
2. Open `index.html` in your browser, or use a local server:
   ```bash
   npx serve .
   ```
3. The dashboard will display real-time updates from your Firebase Realtime Database.

## Project Structure

```
Simulation/
├── index.html
├── README.md
├── .gitignore
├── firebase.json
└── ...other files
```

## Data Structure

- `/logs`: Real-time event logs
- `/storedLogs`: Persistent logs for dashboard display

## Customization

- Modify UI and logic in `index.html` as needed.
- Update event mappings in the JavaScript section to match your system.

## License

MIT License

---
*For questions or contributions, please open an issue or pull request.*