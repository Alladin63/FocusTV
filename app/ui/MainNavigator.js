import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import HomeScreen from './screens/HomeScreen';
import LiveScreen from './screens/LiveScreen';
import VodScreen from './screens/VodScreen';
import SeriesScreen from './screens/SeriesScreen';
import SourcesScreen from './screens/SourcesScreen';
import SettingsScreen from './screens/SettingsScreen';
import PlayerScreen from './screens/PlayerScreen';

const Stack = createNativeStackNavigator();

export default function MainNavigator() {
  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName="Home" screenOptions={{ headerShown: false, animation: 'fade' }}>
        <Stack.Screen name="Home" component={HomeScreen} />
        <Stack.Screen name="Live" component={LiveScreen} />
        <Stack.Screen name="Vod" component={VodScreen} />
        <Stack.Screen name="Series" component={SeriesScreen} />
        <Stack.Screen name="Sources" component={SourcesScreen} />
        <Stack.Screen name="Settings" component={SettingsScreen} />
        <Stack.Screen name="Player" component={PlayerScreen} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
