import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export default function Screen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Séries</Text>
      <Text style={styles.subtitle}>SUBSéries</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#050816', alignItems: 'center', justifyContent: 'center' },
  title: { color: '#ffffff', fontSize: 42, fontWeight: 'bold', marginBottom: 12 },
  subtitle: { color: '#9ca3af', fontSize: 20 },
});
