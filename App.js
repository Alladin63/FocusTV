import React from 'react';
import { View, StatusBar } from 'react-native';
import MainNavigator from './app/ui/MainNavigator';

export default function App() {
  return (
    <View style={{ flex: 1, backgroundColor: '#050816' }}>
      <StatusBar barStyle="light-content" />
      <MainNavigator />
    </View>
  );
}
