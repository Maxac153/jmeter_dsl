#!groovy

import groovy.json.JsonBuilder
import java.text.SimpleDateFormat

def testGenSet = [] as Set
def moduleName = ''
def logPath = ''
def logPathGenOne = ''

class Jar {
    String TEST_FOLDER_PATH
    String MODULE_NAME
    String JAR_NAME
    Double PERCENT_PROFILE = 100.0
}

class CommonSettings {
    Jar JAR
    Map PROPERTIES
}

class Job {
    String GENERATOR
    String TEST_FOLDER
    String TEST_NAME
    String JVM_ARGS
}

class Steps {
    Double TPS
    Double RAMP_TIME
    Double HOLD_TIME
}

class Profile {
    String THREAD_GROUP_NAME
    Integer SCRIPT_EXECUTION_TIME
    Integer PACING_MULTIPLIER
    Integer MAX_THREADS
    Steps[] STEPS
}

class Settings {
    Job JOB
    Profile[] PROFILE
    Map PROPERTIES
}

class CreateScript {
    def static private jsonBuildSettings(Settings settings, Double percentProfile) {
        for (Profile profile in settings.PROFILE) {
            for (Steps step in profile.STEPS) {
                step.TPS = step.TPS * percentProfile / 100.0
            }
        }
        return new JsonBuilder(settings).toString()
    }

    def static private jsonBuildCommonSettings(CommonSettings commonSettings) {
        return new JsonBuilder(commonSettings).toString()
    }

    def static createScript(CommonSettings commonSettings, Settings settings) {
        String script =
                "cd ${commonSettings.JAR.TEST_FOLDER_PATH}COMMON;\n" +
                        "sudo rm -rf logs log test-output\n" +
                        "nohup java ${settings.JOB.JVM_ARGS} -cp ${commonSettings.JAR.JAR_NAME} " +
                        "-DCOMMON_SETTINGS='${jsonBuildCommonSettings(commonSettings)}' -DTEST_SETTINGS='${jsonBuildSettings(settings, commonSettings.JAR.PERCENT_PROFILE)}' " +
                        "org.testng.TestNG -testclass ${settings.JOB.TEST_FOLDER}.${settings.JOB.TEST_NAME}"
        return script
    }
}

// Вывод времени когда тест начался, когда закончится, длительность, время для графаны
def testDuration(Integer wait, Steps[] stepsProfile) {
    SimpleDateFormat sdfDateTime = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy")
    sdfDateTime.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));

    int durationProfile = stepsProfile.inject(0) { acc, step ->
        acc + (step.RAMP_TIME + step.HOLD_TIME) * 60
    } as int

    def calendarStartRumUp = Calendar.getInstance()
    calendarStartRumUp.add(Calendar.SECOND, wait)
    def calendarStart = Calendar.getInstance()
    calendarStart.add(Calendar.SECOND, wait)
    calendarStart.add(Calendar.SECOND, stepsProfile[0].RAMP_TIME * 60 as int)
    def calendarEnd = Calendar.getInstance()
    calendarEnd.add(Calendar.SECOND, wait)
    calendarEnd.add(Calendar.SECOND, durationProfile)
    def diffInMillis = calendarEnd.getTimeInMillis() - calendarStartRumUp.getTimeInMillis()

    long diffInSeconds = (long) (diffInMillis / 1000)
    long diffInMinutes = (long) (diffInSeconds / 60)
    long diffInHours = (long) (diffInMinutes / 60)
    long diffInDays = (long) (diffInHours / 24)

    String delimiter = "|-----------------------------------------------------------|\n"
    String testStartTime = "|  Test Start Time:    ${sdfDateTime.format(calendarStartRumUp.getTime())}"
    String testEndTime = "|  Test End Time:      ${sdfDateTime.format(calendarEnd.getTime())}"
    String testDuration = "|  Test Duration:      ${diffInDays}d ${diffInHours % 24}h:${diffInMinutes % 60}m:${diffInSeconds % 60}s"
    String grafanaRumUp = "|  Grafana (RumUp):    from=${calendarStartRumUp.getTime().getTime()}&to=${calendarEnd.getTime().getTime()}"
    String grafana = "|  Grafana:            from=${calendarStart.getTime().getTime()}&to=${calendarEnd.getTime().getTime()}"

    echo delimiter +
            testStartTime + ' ' * (delimiter.size() - testStartTime.size() - 2) + '|\n' + delimiter +
            testEndTime + ' ' * (delimiter.size() - testEndTime.size() - 2) + '|\n' + delimiter +
            testDuration + ' ' * (delimiter.size() - testDuration.size() - 2) + '|\n' + delimiter +
            grafanaRumUp + ' ' * (delimiter.size() - grafana.size() - 2) + '|\n' + delimiter +
            grafana + ' ' * (delimiter.size() - grafana.size() - 2) + '|\n' + delimiter
}

// Загружаем логи на генератор
def uploadingGenLog(logPath, logFolder, moduleName) {
    SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    sdfDateTime.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"))
    String nowDateTime = sdfDateTime.format(new Date())
    String archiveName = "${moduleName}_${nowDateTime}.tar"

    sshagent(credentials: [env.CREDENTIAL]) {
        sh "tar -cvf ${archiveName} ${logPath}"
        sh "ssh -o 'StrictHostKeyChecking=no' -o 'UserKnownHostsFile=/dev/null' ${env.USERNAME}@${env.GENERATOR_LOGS} 'mkdir -p ${logFolder}'"
        sh "scp -rp ${archiveName} ${env.USERNAME}@${env.GENERATOR_LOGS}:${logFolder}"
    }
}

// Скачивание логов с генератора в домашнюю директорию hitachi_client
def downloadJenkinsLog(logPath, logFolder, generator) {
    try {
        sshagent(credentials: [env.CREDENTIAL]) {
            sh "scp -r ${env.USERNAME}@${generator}:${logPath}/* ${logFolder}"
        }
    } catch (Exception e) {
        echo "ERROR: ${e.toString()}"
    }
}

pipeline {
    agent any
    parameters {
        text(name: 'JSON', description: 'JSON с параметрами запуска', defaultValue: '')
    }

    environment {
        // Креды пользователя для подключения по ssh и scp
        USERNAME = ''
        CREDENTIAL = ''
        GENERATOR_LOGS = 'localhost'
    }

    stages {
        // Запуск тестов
        stage("Starting Tests") {
            when {
                expression { params.JSON != '' }
            }
            steps {
                script {
                    Map<String, List<Object>> testsScripts = [:]
                    def TESTS_PARAM = readJSON text: params.JSON
                    CommonSettings commonSettings = new CommonSettings(TESTS_PARAM.COMMON_SETTINGS)
                    moduleName = commonSettings.JAR.MODULE_NAME
                    logPath = commonSettings.JAR.TEST_FOLDER_PATH + 'COMMON/'
                    logPathGenOne = commonSettings.JAR.TEST_FOLDER_PATH + "LOGS/${moduleName}/"

                    TESTS_PARAM.TESTS_PARAM.eachWithIndex { testParam, index ->
                        Settings settings = new Settings(testParam)
                        testGenSet.add(settings.JOB.GENERATOR)

                        Integer wait = commonSettings.PROPERTIES['WAIT'].toInteger()
                        wait = (wait == null) ? 0 : wait
                        for (Profile profile in settings.PROFILE) {
                            testDuration(wait, profile.STEPS)
                        }

                        String generator = settings.JOB.GENERATOR
                        String testName = "Module: ${commonSettings.JAR.MODULE_NAME} Test: ${settings.JOB.TEST_NAME} #${index + 1}"
                        String script = CreateScript.createScript(commonSettings, settings)
                        echo "${testName}:\n${script}"

                        if (!testsScripts.containsKey(generator))
                            testsScripts[generator] = []

                        testsScripts[generator] << [testName: testName, script: script]
                    }

                    def parallelTasks = [:]
                    testsScripts.each { generator, testsParam ->
                        parallelTasks[generator] = {
                            node(generator) {
                                def parallelStages = testsParam.collectEntries {
                                    [(it): {
                                        stage(it.testName) {
                                            sh it.script
                                        }
                                    }]
                                }
                                parallel parallelStages
                            }
                        }
                    }
                    parallel parallelTasks
                }
            }
        }
    }

    post {
        always {
            script {
                try {
                    echo '=========== CREATE LOG FOLDER JENKINS ==========='
                    String logFolderJenkins = "logs/${moduleName}"
                    sh "mkdir -p ${logFolderJenkins}"
                    testGenSet.each { generator ->
                        downloadJenkinsLog(logPath + "log/${moduleName}", logFolderJenkins, generator)
                    }
                } catch (Exception e) {
                    echo "ERROR: ${e.toString()}"
                }

                try {
                    echo '=========== CREATE LOG FOLDER JENKINS (LOG TEST-OUTPUT) ==========='
                    sh "mkdir -p logs/${moduleName}/test-output"
                    testGenSet.each { generator ->
                        downloadJenkinsLog(logPath + 'test-output', "logs", generator)
                    }
                } catch (Exception e) {
                    echo "ERROR: ${e.toString()}"
                }
            }
        }

        success {
            script {
                try {
                    echo "=========== CREATE LOG FOLDER GENERATOR ONE ==========="
                    uploadingGenLog("logs", logPathGenOne, moduleName)
                } catch (Exception e) {
                    echo "ERROR: ${e.toString()}"
                } finally {
                    archiveArtifacts artifacts: 'logs/**', allowEmptyArchive: true
                    deleteDir()
                }
            }
        }

        unsuccessful {
            script {
                try {
                    echo "=========== STOP JAVA PROCESS TO GENERATORS ==========="
                    testGenSet.each { generator ->
                        node(generator) {
                            sh "sudo kill \$(pgrep -f ${moduleName})"
                        }
                    }
                } catch (Exception e) {
                    echo "ERROR: ${e.toString()}"
                }

                try {
                    echo "=========== CREATE LOG FOLDER GENERATOR ONE ==========="
                    uploadingGenLog("logs", logPathGenOne, moduleName)
                } catch (Exception e) {
                    echo "ERROR: ${e.toString()}"
                } finally {
                    archiveArtifacts artifacts: 'logs/**', allowEmptyArchive: true
                    deleteDir()
                }
            }
        }
    }
}