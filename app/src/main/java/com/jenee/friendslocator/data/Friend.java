package com.jenee.friendslocator.data;

/**
 * Created by jne on 3/21/2018.
 */

public class Friend {
        private int id;
        private String firstName;
        private String lastName;
        private String phone;
        private String location;
        private double latitude;
        private double longitude;
        private byte [] Image;

        public Friend(int id, String firstName, String lastName, String phone, String location, double latitude, double longitude, byte[] image) {
                this.id= id;
                this.firstName = firstName;
                this.lastName = lastName;
                this.phone = phone;
                this.location = location;
                this.latitude = latitude;
                this.longitude = longitude;
                this.Image =image;
        }

        public int getId() {
                return id;
        }

        public void setId(int id) {
                this.id = id;
        }

        public String getFirstName() {
                return firstName;
        }

        public void setFirstName(String firstName) {
                this.firstName = firstName;
        }

        public String getLastName() {
                return lastName;
        }

        public void setLastName(String lastName) {
                this.lastName = lastName;
        }

        public String getPhone() {
                return phone;
        }

        public void setPhone(String phone) {
                this.phone = phone;
        }

        public String getLocation() {
                return location;
        }

        public void setLocation(String location) {
                this.location = location;
        }

        public double getLatitude() {
                return latitude;
        }

        public void setLatitude(double latitude) {
                this.latitude = latitude;
        }

        public double getLongitude() {
                return longitude;
        }

        public void setLongitude(double longitude) {
                this.longitude = longitude;
        }

        public byte[] getImage() {
                return Image;
        }

        public void setImage(byte[] image) {
                Image = image;
        }
}
