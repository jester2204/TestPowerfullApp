package com.jester.testpowerfulapp.screens;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.log4j.Log4j2;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Головний клас програми для аналізу числових послідовностей.
 * Цей клас відповідає за завантаження чисел з файлу, їх аналіз
 * та відображення результатів користувачу.
 */
@Log4j2
@FxmlView
@Component
public class Main {
    // Елементи інтерфейсу користувача
    @FXML public Label timer;
    @FXML public TextField maxValue;
    @FXML public TextField minValue;
    @FXML public TextField median;
    @FXML public TextField average;
    @FXML public ProgressIndicator progress;

    // Список для зберігання чисел
    private final List<Integer> numbers = new ArrayList<>();

    // Змінні для відстеження часу виконання
    private Timeline timeline;
    private int secondsElapsed = 0;

    // Змінні для зберігання результатів аналізу
    private List<Integer> longestIncreasingSequence;
    private List<Integer> longestDecreasingSequence;

    /**
     * Ініціалізує таймер для відстеження часу виконання.
     */
    public void initialize() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    /**
     * Оновлює відображення таймера.
     */
    private void updateTimer() {
        timer.setText(formatTime(++secondsElapsed));
    }

    /**
     * Форматує час у вигляді "хх:хх".
     * @param seconds Кількість секунд
     * @return Відформатований рядок часу
     */
    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    /**
     * Починає аналіз завантажених чисел.
     * Обчислює мінімум, максимум, середнє, медіану та найдовші послідовності.
     */
    public void startAnalyzing(ActionEvent actionEvent) {
        if (isNoNumbersLoaded()) return;

        progress.setVisible(true);
        secondsElapsed = 0;
        timeline.playFromStart();

        Task<Void> analyzeTask = new Task<>() {
            @Override
            protected Void call() {
                int[] minMax = findMinAndMax(numbers);
                double avg = calculateAverage(numbers);
                double med = getMedian(numbers);
                getAnswerDown();
                getAnswerUP();

                Platform.runLater(() -> {
                    maxValue.setText(String.valueOf(minMax[1]));
                    minValue.setText(String.valueOf(minMax[0]));
                    median.setText(String.valueOf(med));
                    average.setText(String.valueOf(avg));
                    progress.setVisible(false);
                    timeline.pause();
                });
                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progress.setVisible(false);
                    timeline.pause();
                });
                log.error("Помилка під час аналізу: {}", getException().getMessage());
            }
        };

        Thread analyzeThread = new Thread(analyzeTask);
        analyzeThread.setDaemon(true);
        analyzeThread.start();
    }

    /**
     * Знаходить мінімальне та максимальне значення в списку.
     * @param numbers Список чисел
     * @return Масив з двох елементів: [мінімум, максимум]
     */
    private int[] findMinAndMax(List<Integer> numbers) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int num : numbers) {
            if (num < min) min = num;
            if (num > max) max = num;
        }
        return new int[]{min, max};
    }

    /**
     * Обчислює середнє арифметичне списку чисел.
     * @param numbers Список чисел
     * @return Середнє арифметичне
     */
    private double calculateAverage(List<Integer> numbers) {
        long sum = 0;
        for (int num : numbers) {
            sum += num;
        }
        return (double) sum / numbers.size();
    }

    /**
     * Обчислює медіану списку чисел.
     * @param numbers Список чисел
     * @return Медіана
     */
    private double getMedian(List<Integer> numbers) {
        int size = numbers.size();
        if (size % 2 == 0) {
            // Якщо розмір списку парний, знаходимо два середні елементи
            int left = quickSelectIterative(new ArrayList<>(numbers), size / 2 - 1);
            int right = quickSelectIterative(new ArrayList<>(numbers), size / 2);
            // Повертаємо середнє арифметичне двох середніх елементів
            return (left + right) / 2.0;
        } else {
            // Якщо розмір списку непарний, знаходимо середній елемент
            return quickSelectIterative(new ArrayList<>(numbers), size / 2);
        }
    }

    /**
     * Реалізує алгоритм швидкого вибору для знаходження k-го найменшого елемента.
     * Цей метод працює ітеративно, уникаючи рекурсії.
     * @param nums Список чисел
     * @param k Індекс шуканого елемента (k-й найменший)
     * @return k-й найменший елемент
     */
    private int quickSelectIterative(List<Integer> nums, int k) {
        int left = 0;
        int right = nums.size() - 1;

        while (true) {
            // Якщо ліва і права межі збігаються, ми знайшли елемент
            if (left == right) return nums.get(left);

            // Розділяємо список і отримуємо індекс опорного елемента
            int pivotIndex = partition(nums, left, right);

            if (k == pivotIndex) {
                // Якщо індекс опорного елемента збігається з k, ми знайшли шуканий елемент
                return nums.get(k);
            } else if (k < pivotIndex) {
                // Якщо k менше індексу опорного, шуканий елемент в лівій частині
                right = pivotIndex - 1;
            } else {
                // Інакше шуканий елемент у правій частині
                left = pivotIndex + 1;
            }
        }
    }

    /**
     * Допоміжний метод для алгоритму швидкого вибору.
     * Розділяє список на дві частини відносно опорного елемента.
     * @param nums Список для розділення
     * @param left Ліва межа частини списку для розділення
     * @param right Права межа частини списку для розділення
     * @return Індекс опорного елемента після розділення
     */
    private int partition(List<Integer> nums, int left, int right) {
        // Обираємо останній елемент як опорний
        int pivot = nums.get(right);
        int i = left - 1;

        // Проходимо по елементах, переміщуючи елементи менше опорного вліво
        for (int j = left; j < right; j++) {
            if (nums.get(j) <= pivot) {
                i++;
                Collections.swap(nums, i, j);
            }
        }

        // Розміщуємо опорний елемент на його правильну позицію
        Collections.swap(nums, i + 1, right);

        // Повертаємо індекс опорного елемента
        return i + 1;
    }

    /**
     * Знаходить найдовшу зростаючу послідовність.
     */
    public void getAnswerUP() {
        if (isNoNumbersLoaded()) return;
        longestIncreasingSequence = findLongestSequence(numbers, true);
    }

    /**
     * Знаходить найдовшу спадну послідовність.
     */
    public void getAnswerDown() {
        if (isNoNumbersLoaded()) return;
        longestDecreasingSequence = findLongestSequence(numbers, false);
    }

    /**
     * Знаходить найдовшу послідовність (зростаючу або спадну).
     * @param numbers Список чисел
     * @param increasing true для зростаючої, false для спадної послідовності
     * @return Найдовша послідовність
     */
    private List<Integer> findLongestSequence(List<Integer> numbers, boolean increasing) {
        int maxLength = 0;
        int endIndex = 0;
        int currentLength = 1;

        for (int i = 1; i < numbers.size(); i++) {
            if ((increasing && numbers.get(i) > numbers.get(i - 1)) ||
                    (!increasing && numbers.get(i) < numbers.get(i - 1))) {
                currentLength++;
                if (currentLength > maxLength) {
                    maxLength = currentLength;
                    endIndex = i;
                }
            } else {
                currentLength = 1;
            }
        }

        List<Integer> result = new ArrayList<>(maxLength);
        for (int i = endIndex - maxLength + 1; i <= endIndex; i++) {
            result.add(numbers.get(i));
        }
        return result;
    }

    /**
     * Завантажує числа з текстового файлу.
     */
    public void loadTxtFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстові файли", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            progress.setVisible(true);
            Task<Void> loadFileTask = new Task<>() {
                @Override
                protected Void call() throws IOException {
                    numbers.clear();
                    try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (!line.trim().isEmpty()) {
                                numbers.add(Integer.parseInt(line.trim()));
                            }
                        }
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        progress.setVisible(false);
                        log.info("Файл успішно завантажено. Всього чисел: " + numbers.size());
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        progress.setVisible(false);
                        log.error("Помилка завантаження файлу: {}", getException().getMessage());
                    });
                }
            };

            Thread loadFileThread = new Thread(loadFileTask);
            loadFileThread.setDaemon(true);
            loadFileThread.start();
        } else {
            log.info("Вибір файлу скасовано.");
        }
    }

    /**
     * Перевіряє, чи завантажено числа.
     * @return true, якщо числа не завантажено
     */
    private boolean isNoNumbersLoaded(){
        if (numbers.isEmpty()) {
            log.info("Числа не завантажено.");
            Alert noNumbersLoaded = new Alert(Alert.AlertType.ERROR, "Числа не завантажено");
            noNumbersLoaded.showAndWait();
            return true;
        }
        return false;
    }

    /**
     * Відображає найдовшу зростаючу послідовність.
     */
    public void getAnswerIncrease(ActionEvent actionEvent) {
        new Alert(Alert.AlertType.INFORMATION, """
                Найдовша зростаюча послідовність:
                
                %s""".formatted(longestIncreasingSequence) ).showAndWait();
    }

    /**
     * Відображає найдовшу спадну послідовність.
     */
    public void getAnswerDecrease(ActionEvent actionEvent) {
        new Alert(Alert.AlertType.INFORMATION, """
                Найдовша спадна послідовність:
                
                %s""".formatted(longestDecreasingSequence) ).showAndWait();
    }
}