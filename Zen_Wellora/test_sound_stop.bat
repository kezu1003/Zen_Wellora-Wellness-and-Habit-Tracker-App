@echo off
echo Building app with NUCLEAR sound stopping...

cd /d "C:\Users\ASUS Vivobook\AndroidStudioProjects\Zen_Wellora"

echo.
echo Cleaning project...
call gradlew.bat clean

echo.
echo Building debug APK...
call gradlew.bat assembleDebug

echo.
echo Installing app...
adb install -r app\build\outputs\apk\debug\app-debug.apk

echo.
echo Clearing logcat buffer...
adb logcat -c

echo.
echo Starting logcat for debugging (showing only our tags)...
echo Press Ctrl+C to stop logcat
echo.
echo TESTING INSTRUCTIONS:
echo 1. Trigger a hydration notification in the app
echo 2. Wait for sound to start playing
echo 3. Click "I Drank Water" in the notification
echo 4. Watch logs below - sound should stop immediately!
echo.
adb logcat -s AlarmSoundManager:D HydrationAction:D NotificationHelper:D

pause
