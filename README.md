# SpaceX Launch Tracker 🚀  

![App Demo](https://media2.giphy.com/media/v1.Y2lkPTc5MGI3NjExajJuY25kbGp6NXZyb3d2cTUweDU0NTRlNW53c3Z6cXdiOHljZTY0OCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/1WYdTVlHUgclTnUvvH/giphy.gif) 

## Setup Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/spacex-launches.git
   cd spacex-launches

Open the project in Android Studio.  
Build the project to download dependencies:  
./gradlew build
Run the app on an emulator or physical device.

##### Features implemented

## Features Implemented

- Fetches real-time data from the SpaceX API.
- Displays upcoming and past SpaceX rocket launches.
- Includes countdowns and launch details.
- Sleek, responsive UI with navigation and tabs.

## Technologies Used

- Kotlin
- Retrofit
- ViewModel and LiveData
- RecyclerView
- ConstraintLayout
- Picasso
- Android Navigation Component

## Challenges Faced and Solutions

- **API Integration**: Faced issues with API response parsing. Solved by ensuring correct data models and using Gson converter.
- **Navigation**: Initial navigation setup was complex. Solved by following Android Navigation Component documentation.
- **UI Responsiveness**: Ensured UI elements adapt to different screen sizes using ConstraintLayout and proper padding/margins.

## Future Improvements

- Add more detailed launch information.
- Implement search functionality for launches.
- Improve UI/UX with animations and better design.
- Add offline support using Room database.
