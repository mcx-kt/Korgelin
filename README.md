# Korgelin
Fork of
[Shadowfacts's Forgelin](https://github.com/shadowfacts/Forgelin).

**For detail: Please see
[wiki](https://github.com/toliner/Korgelin/wiki)**

## Additions
- Shades kotlinx-serialization-runtime
- Support 1.13 and older version.

## Usage
```groovy
repositories {
	jcenter()
	maven { url "https://dl.bintray.com/toliner/Korgelin" }
}

dependencies {
	compile group: "net.toliner", name: "Korgelin", version: "LATEST_VERSION"
}
```

All versions can be seen [here](https://bintray.com/toliner/Korgelin).

**Note:** You must have the `jcenter()` call in your `repositories` block. JCenter is used to host the Kotlin coroutines libraries.
