package com.t13max.game.pos;

import java.util.Random;

public class Matrix {
    public int row;
    public int rank;
    public double[][] mat;

    public Matrix(int a, int b) {
        row = a;
        rank = b;
        mat = new double[row][rank];
    }

    public void New() {
        Random rand = new Random();
        for (int i = 0; i < row; i++)
            for (int j = 0; j < rank; j++)
                mat[i][j] = rand.nextInt(100);
    }

    public void init(double[][] in) {
        mat = in;
    }

    public void Output() {
        System.out.println("Matrix=:");
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < rank; j++)
                System.out.print(mat[i][j] + " ");
            System.out.println();
        }
        System.out.println();
    }

    public Matrix Plus(Matrix a) {
        Matrix c = new Matrix(row, rank);
        if (a.row == row && a.rank == rank) {
            for (int i = 0; i < row; i++)
                for (int j = 0; j < rank; j++)
                    c.mat[i][j] = mat[i][j] + a.mat[i][j];
        } else {
            System.out.println("matrixAdd error!");
        }
        return c;
    }

    public Matrix Minus(Matrix a) {
        Matrix c = new Matrix(row, rank);
        if (a.row == row && a.rank == rank) {
            for (int i = 0; i < row; i++)
                for (int j = 0; j < rank; j++)
                    c.mat[i][j] = mat[i][j] - a.mat[i][j];
        } else {
            System.out.println("matrixMinus error!");
        }
        return c;
    }

    public Matrix mMultiply(double a) {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < rank; j++) {
                mat[i][j] *= a;
            }
        }
        return this;
    }

    public Matrix Multiply(Matrix a) {
        Matrix c = new Matrix(row, a.rank);
        if (rank == a.row) {
            for (int i = 0; i < c.row; i++)
                for (int j = 0; j < c.rank; j++) {
                    double sum = 0;
                    for (int k = 0; k < rank; k++)
                        sum += mat[i][k] * a.mat[k][j];
                    c.mat[i][j] = sum;
                }
        } else {
            System.out.println("matrix Multiply errors!");
        }
        return c;
    }

    public Matrix Trans() {
        Matrix c = new Matrix(rank, row);
        for (int i = 0; i < c.row; i++)
            for (int j = 0; j < c.rank; j++)
                c.mat[i][j] = mat[j][i];
        return c;
    }

    public Matrix Invs() {
        int rw = row, rk = rank;
        Matrix imat = new Matrix(rw, rk);
        Matrix jmat = new Matrix(rw, rk);
        for (int i = 0; i < rw; i++)
            for (int j = 0; j < rw; j++)
                jmat.mat[i][j] = mat[i][j];
        for (int i = 0; i < rw; i++)
            for (int j = 0; j < rw; j++)
                imat.mat[i][j] = 0;
        for (int i = 0; i < rw; i++)
            imat.mat[i][i] = 1;

        for (int i = 0; i < rw; i++) {
            for (int j = 0; j < rw; j++) {
                if (i != j) {
                    double t = jmat.mat[j][i] / jmat.mat[i][i];
                    for (int k = 0; k < rw; k++) {
                        jmat.mat[j][k] -= jmat.mat[i][k] * t;
                        imat.mat[j][k] -= imat.mat[i][k] * t;
                    }
                }
            }
        }
        for (int i = 0; i < rw; i++)
            if (jmat.mat[i][i] != 1) {
                double t = jmat.mat[i][i];
                for (int j = 0; j < rw; j++) {
                    jmat.mat[i][j] = jmat.mat[i][j] / t;
                    imat.mat[i][j] = imat.mat[i][j] / t;
                }
            }
        return imat;
    }
}
