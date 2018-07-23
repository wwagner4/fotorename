package frn;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class Main {

    public static void main(String[] args) throws IOException {
        new Main().run();
    }

    private void run() throws IOException {
        System.out.println("STARTED");
        Path inp = Paths.get("/Users/wwagner4/Pictures/2018/Urlaub Frankreich");
        Path outp = Paths.get("/Users/wwagner4/Pictures/2018/Urlaub Frankreich sorted");
        deleteDir(outp);
        Files.createDirectories(outp);
        if (!Files.exists(inp)) throw new IllegalArgumentException("In-Path " + inp + " must exist");
        List<Pic> pics = Files.list(inp)
                .filter(f -> Files.isRegularFile(f))
                .filter(f -> f.getFileName().toString().toLowerCase().endsWith("jpg"))
                .map(this::toPic)
                .sorted(Comparator.comparing(Pic::getDate))
                .collect(Collectors.toList());
        int nr = 1;
        for (Pic pic : pics) {
            String name = String.format("FR2018_%04d.jpg", nr);
            Path des = outp.resolve(name);
            System.out.println(pic.getPath() + " -> " + des);
            Files.copy(pic.getPath(), des);
            nr++;
        }
        System.out.println("FINISHED " + outp);

    }

    private void deleteDir(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(this::deleteFile);
        }
    }

    private void deleteFile(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new IllegalStateException("File " + file + " could not be deleted. " + e.getMessage(), e);
        }
    }

    private Pic toPic(Path path)  {
        try {
            String date = extractDate(path);
            return new Pic(path, date);
        } catch (ImageProcessingException | IOException e) {
            throw new IllegalStateException("Error extracting metadata for " + path, e);
        }
    }

    private String extractDate(Path path) throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
        String ts = "-";
        for (Directory directory : metadata.getDirectories()) {
            if ("Exif IFD0".equals(directory.getName())) {
                Collection<Tag> tags = directory.getTags();
                for (Tag tag : tags) {
                    if ("Date/Time".equals(tag.getTagName())) {
                        ts = tag.getDescription();
                    }
                }
            }
        }
        return ts;
    }

}

class Pic {

    private final Path path;
    private final String date;

    Pic(Path path, String date) {
        this.path = path;
        this.date = date;
    }

    Path getPath() {
        return path;
    }

    String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Pic{" +
                "path=" + path +
                ", date='" + date + '\'' +
                '}';
    }
}
