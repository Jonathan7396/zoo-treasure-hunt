import React, { useState, useEffect } from "react";
import {
  StyleSheet,
  Text,
  View,
  FlatList,
  TouchableOpacity,
  Image,
  Alert,
  ActivityIndicator,
} from "react-native";

import AsyncStorage from "@react-native-async-storage/async-storage";
import * as Location from "expo-location";
import * as ImagePicker from "expo-image-picker";

const DEFAULT_SIGHTINGS = [
  {
    id: "1",
    name: "Lion",
    lat: -35.0066975,
    lng: 138.5602552,
    isFound: false,
    photo: null,
  },
  {
    id: "2",
    name: "Red Panda",
    lat: -34.9128,
    lng: 138.6065,
    isFound: false,
    photo: null,
  },
  {
    id: "3",
    name: "Giraffe",
    lat: -34.913,
    lng: 138.6068,
    isFound: false,
    photo: null,
  },
  {
    id: "4",
    name: "Kangaroo",
    lat: -34.9132,
    lng: 138.607,
    isFound: false,
    photo: null,
  },
  {
    id: "5",
    name: "Penguin",
    lat: -34.9134,
    lng: 138.6072,
    isFound: false,
    photo: null,
  },
];

export default function App() {
  const [sightings, setSightings] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSightings();
  }, []);

  const loadSightings = async () => {
    try {
      const savedData = await AsyncStorage.getItem("@sightings_key");

      if (savedData !== null) {
        setSightings(JSON.parse(savedData));
      } else {
        setSightings(DEFAULT_SIGHTINGS);
      }
    } catch (e) {
      console.error("Failed to load data", e);
      setSightings(DEFAULT_SIGHTINGS);
    } finally {
      setLoading(false);
    }
  };

  const saveSightings = async (updatedList) => {
    setSightings(updatedList);

    try {
      await AsyncStorage.setItem("@sightings_key", JSON.stringify(updatedList));
    } catch (e) {
      console.error("Failed to save data", e);
    }
  };

  const handleSightingPress = async (item) => {
    if (item.isFound) {
      Alert.alert(item.name, "You already found this animal!");
      return;
    }

    try {
      const { status: locStatus } =
        await Location.requestForegroundPermissionsAsync();
      const { status: camStatus } =
        await ImagePicker.requestCameraPermissionsAsync();

      if (locStatus !== "granted" || camStatus !== "granted") {
        Alert.alert(
          "Permission Required",
          "This app needs Location and Camera permissions to verify sightings.",
        );
        return;
      }

      const location = await Location.getCurrentPositionAsync({});
      const distance = getDistance(
        location.coords.latitude,
        location.coords.longitude,
        item.lat,
        item.lng,
      );

      if (distance > 50) {
        Alert.alert(
          "Too Far!",
          `You are ${Math.round(distance)}m away. You must be within 50m of the exhibit.`,
        );
        return;
      }

      const result = await ImagePicker.launchCameraAsync({
        allowsEditing: true,
        quality: 0.5,
      });

      if (!result.canceled) {
        const updated = sightings.map((s) =>
          s.id === item.id
            ? { ...s, isFound: true, photo: result.assets[0].uri }
            : s,
        );

        await saveSightings(updated);
        Alert.alert("Success!", `You found the ${item.name}!`);
      }
    } catch (error) {
      console.error(error);
      Alert.alert(
        "Error",
        "Something went wrong while verifying the sighting.",
      );
    }
  };

  const getDistance = (lat1, lon1, lat2, lon2) => {
    const R = 6371e3;
    const phi1 = (lat1 * Math.PI) / 180;
    const phi2 = (lat2 * Math.PI) / 180;
    const deltaPhi = ((lat2 - lat1) * Math.PI) / 180;
    const deltaLambda = ((lon2 - lon1) * Math.PI) / 180;

    const a =
      Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
      Math.cos(phi1) *
        Math.cos(phi2) *
        Math.sin(deltaLambda / 2) *
        Math.sin(deltaLambda / 2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
  };

  const resetProgress = () => {
    Alert.alert("Reset", "Clear all progress?", [
      { text: "Cancel", style: "cancel" },
      {
        text: "Yes",
        style: "destructive",
        onPress: () => saveSightings(DEFAULT_SIGHTINGS),
      },
    ]);
  };

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" />
        <Text style={styles.loadingText}>Loading sightings...</Text>
      </View>
    );
  }

  const foundCount = sightings.filter((item) => item.isFound).length;

  return (
    <View style={styles.container}>
      <Text style={styles.header}>Zoo Treasure Hunt</Text>

      <View style={styles.progressBox}>
        <Text style={styles.progressTitle}>Safari Progress</Text>
        <Text style={styles.progressText}>
          {foundCount} of {sightings.length} animals found
        </Text>
      </View>

      <FlatList
        data={sightings}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <TouchableOpacity
            style={[styles.card, item.isFound && styles.cardFound]}
            onPress={() => handleSightingPress(item)}
          >
            <View style={styles.cardContent}>
              <View style={styles.cardText}>
                <Text style={styles.animalName}>{item.name}</Text>
                <Text style={styles.statusText}>
                  {item.isFound ? "✅ Found" : "🔍 Tap to verify location"}
                </Text>
              </View>

              {item.photo ? (
                <Image source={{ uri: item.photo }} style={styles.thumbnail} />
              ) : (
                <View style={styles.placeholderCircle}>
                  <Text style={styles.placeholderText}>📷</Text>
                </View>
              )}
            </View>
          </TouchableOpacity>
        )}
      />

      <TouchableOpacity style={styles.resetBtn} onPress={resetProgress}>
        <Text style={styles.resetText}>Reset Progress</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#ffffff",
    paddingTop: 60,
    paddingHorizontal: 20,
  },
  header: {
    fontSize: 28,
    fontWeight: "900",
    color: "#2c3e50",
    marginBottom: 16,
  },
  progressBox: {
    backgroundColor: "#eef6ff",
    padding: 16,
    borderRadius: 14,
    marginBottom: 18,
  },
  progressTitle: {
    fontSize: 16,
    fontWeight: "700",
    color: "#2c3e50",
  },
  progressText: {
    marginTop: 4,
    fontSize: 14,
    color: "#6c757d",
  },
  card: {
    backgroundColor: "#f8f9fa",
    padding: 20,
    borderRadius: 15,
    marginBottom: 15,
    borderWidth: 1,
    borderColor: "#e9ecef",
  },
  cardFound: {
    backgroundColor: "#d4edda",
    borderColor: "#c3e6cb",
  },
  cardContent: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  cardText: {
    flex: 1,
    paddingRight: 12,
  },
  animalName: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#343a40",
  },
  statusText: {
    fontSize: 14,
    color: "#6c757d",
    marginTop: 4,
  },
  thumbnail: {
    width: 56,
    height: 56,
    borderRadius: 28,
  },
  placeholderCircle: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: "#e9ecef",
    justifyContent: "center",
    alignItems: "center",
  },
  placeholderText: {
    fontSize: 24,
  },
  centered: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  loadingText: {
    marginTop: 10,
    color: "#6c757d",
  },
  resetBtn: {
    marginTop: 10,
    marginBottom: 30,
    padding: 10,
    alignSelf: "center",
  },
  resetText: {
    color: "#dc3545",
    fontWeight: "700",
  },
});
