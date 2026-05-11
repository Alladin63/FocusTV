import React from 'react';
import { View, Text, StyleSheet, Pressable } from 'react-native';

export default function SourcesScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Sources IPTV</Text>
      <Text style={styles.subtitle}>Sources M3U, Xtream Codes et MAC/Stalker.</Text>

      <View style={styles.row}>
        <Pressable focusable style={({ focused }) => [styles.button, focused && styles.focused]}>
          <Text style={styles.buttonText}>Ajouter M3U</Text>
        </Pressable>
        <Pressable focusable style={({ focused }) => [styles.button, focused && styles.focused]}>
          <Text style={styles.buttonText}>Ajouter Xtream</Text>
        </Pressable>
        <Pressable focusable style={({ focused }) => [styles.button, focused && styles.focused]}>
          <Text style={styles.buttonText}>Ajouter MAC</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#050816', padding: 60, justifyContent: 'center' },
  title: { color: '#ffffff', fontSize: 44, fontWeight: 'bold', marginBottom: 14 },
  subtitle: { color: '#cbd5e1', fontSize: 20, marginBottom: 35 },
  row: { flexDirection: 'row', gap: 18 },
  button: { backgroundColor: 'rgba(255,255,255,0.07)', borderRadius: 18, paddingVertical: 18, paddingHorizontal: 28 },
  focused: { backgroundColor: '#2563eb', transform: [{ scale: 1.06 }] },
  buttonText: { color: '#ffffff', fontSize: 20, fontWeight: '700' },
});
