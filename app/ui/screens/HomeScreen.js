import React from 'react';
import { View, Text, StyleSheet, Pressable } from 'react-native';

const MENU = [
  { label: 'Live TV', screen: 'Live' },
  { label: 'Films', screen: 'Vod' },
  { label: 'Séries', screen: 'Series' },
  { label: 'Sources IPTV', screen: 'Sources' },
  { label: 'Paramètres', screen: 'Settings' },
];

export default function HomeScreen({ navigation }) {
  return (
    <View style={styles.container}>
      <View style={styles.sidebar}>
        <Text style={styles.logo}>FocusTV</Text>
        {MENU.map((item) => (
          <Pressable
            key={item.screen}
            focusable={true}
            hasTVPreferredFocus={item.screen === 'Live'}
            onPress={() => navigation.navigate(item.screen)}
            style={({ focused }) => [styles.menuButton, focused && styles.menuButtonFocused]}
          >
            {({ focused }) => (
              <Text style={[styles.menuText, focused && styles.menuTextFocused]}>{item.label}</Text>
            )}
          </Pressable>
        ))}
      </View>

      <View style={styles.hero}>
        <Text style={styles.title}>FocusTV</Text>
        <Text style={styles.subtitle}>Interface IPTV TV/Firestick avec navigation télécommande.</Text>
        <Text style={styles.hint}>Haut, bas, gauche, droite, OK et retour.</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, flexDirection: 'row', backgroundColor: '#050816' },
  sidebar: { width: 280, paddingTop: 50, paddingHorizontal: 24, backgroundColor: '#020617' },
  logo: { color: '#ffffff', fontSize: 34, fontWeight: 'bold', marginBottom: 35 },
  menuButton: {
    paddingVertical: 16,
    paddingHorizontal: 18,
    borderRadius: 16,
    marginBottom: 12,
    backgroundColor: 'rgba(255,255,255,0.04)',
  },
  menuButtonFocused: { backgroundColor: '#2563eb', transform: [{ scale: 1.04 }] },
  menuText: { color: '#cbd5e1', fontSize: 20, fontWeight: '600' },
  menuTextFocused: { color: '#ffffff' },
  hero: { flex: 1, padding: 60, justifyContent: 'center' },
  title: { color: '#ffffff', fontSize: 62, fontWeight: 'bold', marginBottom: 16 },
  subtitle: { color: '#cbd5e1', fontSize: 24, maxWidth: 800, lineHeight: 34 },
  hint: { color: '#94a3b8', fontSize: 18, marginTop: 30 },
});
