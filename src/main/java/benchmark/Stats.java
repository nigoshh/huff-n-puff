package benchmark;

import domain.BitSequence;
import domain.CompressionAlgorithm;
import io.FileUtils;
import io.IO;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import static ui.commands.Compare.FILE_COMP_DEC_E;
import static ui.commands.Compare.FILE_R_ERROR;
import static ui.commands.Compare.FILE_W_ERROR;
import static ui.commands.Compare.WITH;

/**
 * Represents some stats about a compression algorithm; contains also static
 * methods to compute such stats.
 */
public class Stats {

    public long uncompressedSizeInBytes;
    public long compressedSizeInBits;
    public long compressionElapsedTime;
    public long decompressionElapsedTime;

    /**
     * Returns the compression rate measured in bits per symbol (bps), which is
     * equal to the quotient of the size of the compressed data in bits to the
     * size of the uncompressed data in bytes (for more info see
     * http://www.data-compression.info/Corpora/).
     *
     * @return The compression rate measured in bits per symbol (bps).
     */
    public double bitsPerSymbol() {
        return (double) compressedSizeInBits / uncompressedSizeInBytes;
    }

    /**
     * Returns the data compression ratio, which is the ratio between
     * uncompressed size and compressed size (for more info see
     * https://en.wikipedia.org/wiki/Data_compression_ratio).
     *
     * @return The data compression ratio.
     */
    public double compressionRatio() {
        return uncompressedSizeInBytes * Byte.SIZE / (double) compressedSizeInBits;
    }

    /**
     * Computes stats about the given compression algorithm.
     *
     * @param algorithm Compression algorithm to be used.
     * @param originalFilePath Path of the file to be compressed.
     * @param originalData Content of the file to be compressed.
     * @param reps How many times the file will be compressed and decompressed.
     * @param io An IO used to print possible error messages.
     * @return Stats about the given compression algorithm.
     */
    public static Stats computeStats(CompressionAlgorithm algorithm,
            Path originalFilePath, byte[] originalData, int reps, IO io) {

        Stats stats = new Stats();

        stats.uncompressedSizeInBytes = originalData.length;

        BitSequence compressedDataBitSeq;
        int i = 0;
        long compressionStartingTime = System.nanoTime();
        do {
            compressedDataBitSeq = algorithm.compressData(originalData);
        } while (++i < reps);
        stats.compressionElapsedTime = System.nanoTime() - compressionStartingTime;

        stats.compressedSizeInBits = compressedDataBitSeq.getLengthInBits();

        Path compressedFilePath = originalFilePath.resolveSibling(
                originalFilePath.getFileName() + algorithm.getExtension());

        if (!FileUtils.writeFile(compressedFilePath, compressedDataBitSeq.getBits(),
                compressedDataBitSeq.getLengthInBytes())) {
            io.println(FILE_W_ERROR + compressedFilePath + "\n");
            return null;
        }

        byte[] compressedData = FileUtils.readFile(compressedFilePath);
        if (compressedData == null) {
            io.println(FILE_R_ERROR + compressedFilePath + "\n");
            return null;
        }

        byte[] decompressedData;
        long decompressionStartingTime = System.nanoTime();
        do {
            decompressedData = algorithm.decompressData(compressedData);
        } while (--i > 0);
        stats.decompressionElapsedTime = System.nanoTime() - decompressionStartingTime;

        if (!Arrays.equals(originalData, decompressedData)) {
            io.println(FILE_COMP_DEC_E + originalFilePath + WITH + algorithm.getName() + "\n");
            return null;
        }

        return stats;
    }

    /**
     * Returns a Stats object in which each variable is equal to the sum of the
     * values it gets in each of the Stats object in the given array.
     *
     * @param stats The Stats objects whose variables' values will be summed.
     * @return A Stats object in which each variable is equal to the sum of the
     * values it gets in each of the Stats object in the given array.
     */
    public static Stats[] sumStats(Stats[][] stats) {

        Stats[] summaryStats = new Stats[stats.length];
        for (int i = 0; i < summaryStats.length; i++) {

            summaryStats[i] = Stream.of(stats[i]).reduce(new Stats(), (s1, s2) -> {
                s1.uncompressedSizeInBytes += s2.uncompressedSizeInBytes;
                s1.compressedSizeInBits += s2.compressedSizeInBits;
                s1.compressionElapsedTime += s2.compressionElapsedTime;
                s1.decompressionElapsedTime += s2.decompressionElapsedTime;
                return s1;
            });
        }

        return summaryStats;
    }
}
